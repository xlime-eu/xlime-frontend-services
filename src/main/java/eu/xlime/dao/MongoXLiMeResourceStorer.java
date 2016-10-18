package eu.xlime.dao;

import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.mongojack.DBCursor;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;

import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.mongo.DBCollectionProvider;
import eu.xlime.util.XLiMeResourceTyper;
import eu.xlime.util.score.Score;
import eu.xlime.util.score.ScoreFactory;
import eu.xlime.util.score.ScoredSet;
import eu.xlime.util.score.ScoredSetImpl;

public class MongoXLiMeResourceStorer implements XLiMeResourceStorer {
	private static final Logger log = LoggerFactory.getLogger(MongoXLiMeResourceStorer.class);

	private final static XLiMeResourceTyper resTyper = new XLiMeResourceTyper();
	private final DBCollectionProvider collectionProvider;
    private static final ExecutorService exec = Executors.newFixedThreadPool(4);
	private static final ScoreFactory scoref = ScoreFactory.instance;

	public MongoXLiMeResourceStorer(Properties props){
		this(new DBCollectionProvider(props));
	}

	public MongoXLiMeResourceStorer(DBCollectionProvider aCollectionProvider){
		collectionProvider = aCollectionProvider;
	}

	@Override
	public <T extends XLiMeResource> String insertOrUpdate(T xLiMeRes) {
		return insertOrUpdate(xLiMeRes, Optional.<Locale>absent());
	}
	
	@Override
	public <T extends XLiMeResource> String insertOrUpdate(T xLiMeRes,
			Optional<Locale> optLocale) {
		if (xLiMeRes.getUrl() == null) throw new IllegalArgumentException("Cannot store xLiMe resource with null url." + xLiMeRes);
		JacksonDBCollection<T, String> coll = getCollection(xLiMeRes, optLocale);
		if (log.isDebugEnabled()) {
			log.debug(String.format("Collection %s (%s docs) before upserting %s", coll.getName(), coll.count(), xLiMeRes.getUrl()));
		}
		String id = null;
		String op = "inserting";
		try {
			WriteResult<T, String> result = coll.insert(xLiMeRes);
			id = result.getSavedId();
		} catch (DuplicateKeyException e) {
			//object already in, update instead
			WriteResult<T, String> result = coll.updateById(xLiMeRes.getUrl(), xLiMeRes);
			id = xLiMeRes.getUrl();
			op = "updating";
		}
		if (log.isDebugEnabled()) {
			log.debug(String.format("Collection %s (%s docs) after %s, saved id %s", coll.getName(), coll.count(), op, xLiMeRes.getUrl(), id));
		}
		return id;
	}

	@Override
	public <T extends XLiMeResource> long count(Class<T> xLiMeResClass) {
		return count(xLiMeResClass, Optional.<Locale>absent());
	}
	
	@Override
	public <T extends XLiMeResource> long count(Class<T> xLiMeResClass,
			Optional<Locale> optLocale) {
		JacksonDBCollection<T, String> coll = getDBCollection(xLiMeResClass, optLocale);
		return coll.count();
	}

	@Override
	public <T extends XLiMeResource> Optional<T> findResource(
			Class<T> resourceClass, String resourceUrl) {
		return findResource(resourceClass, Optional.<Locale>absent(), resourceUrl);
	}

	@Override
	public <T extends XLiMeResource> Optional<T> findResource(
			Class<T> resourceClass, Optional<Locale> optLocale,
			String resourceUrl) {
		DBCursor<T> cursor = getDBCollection(resourceClass, optLocale).find().in("_id", ImmutableList.of(resourceUrl));
		if (cursor.count() > 1)
			log.debug("Expecting at most one result, but found " + cursor.count() + " returning first.");
		return cursor.hasNext() ? Optional.of(cursor.next()) : Optional.<T>absent();
	}

	public <T extends XLiMeResource> JacksonDBCollection<T, String> getCollection(
			T mediaItem) {
		return getCollection(mediaItem, Optional.<Locale>absent());
	}
	
	public <T extends XLiMeResource> JacksonDBCollection<T, String> getCollection(
			T mediaItem, Optional<Locale> optLoc) {
		Class<T> miClass = resTyper.findResourceClass(mediaItem);
		return getDBCollection(miClass, optLoc);
	}

	public <T extends XLiMeResource> JacksonDBCollection<T, String> getDBCollection(
			Class<T> miClass) {
		return getDBCollection(miClass, Optional.<Locale>absent());
	}
	public <T extends XLiMeResource> JacksonDBCollection<T, String> getDBCollection(
				Class<T> miClass, Optional<Locale> optLoc) {
		JacksonDBCollection<T, String> coll = JacksonDBCollection.wrap(collectionProvider.getDBCollection(miClass, optLoc), miClass,
		        String.class);
		return coll;
	}
	
	public <T extends XLiMeResource> ScoredSet<T> toScoredSet(DBCursor<T> cursor, int limit, String justif, String scoreField) {
		return toScoredSet(cursor, limit, justif, scoreField, 2000L); //TODO: read value from configuration properties in constructor
	}

	public <T extends XLiMeResource> ScoredSet<T> toScoredSet(DBCursor<T> cursor, int limit, String justif, String scoreField, long timeout) {
		ScoredSet.Builder<T> builder = ScoredSetImpl.builder();
		long start = System.currentTimeMillis();
		List<T> list = execute(cursor, limit, timeout);
		log.debug("Executed query in " + (System.currentTimeMillis() - start) + "ms.");
		for (T bean: list) {
			builder.add(bean, toScore(bean, justif));
		}
		return builder.build();
	}

	private <T> Score toScore(T bean, String justif) {
		//Can we add a 'score' item to each XLiMeResource and use that?
		if (justif != null)
			return scoref.newScore(1.0, justif);
		else return scoref.newScore(1.0);
	}

	public <T> List<T> execute(
			DBCursor<T> cursor, int limit, long timeOut) {
		Future<List<T>> task = exec.submit(callableExecute(cursor, limit));
		try {
			return task.get(timeOut, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		} catch (TimeoutException e) {
			log.debug(String.format("Failed to execute query %s on %s in time", cursor.getQuery(), cursor.getCollection().getName()));
			//TODO: do something more interesting, e.g. wait for completion and put in cache
			// to avoid calling executing again next time...
			try {
				task.cancel(true);
			} catch (Exception e1) {
				//ignore
			}
			return ImmutableList.of();
		}
	}

	public <T extends XLiMeResource> List<T> getSortedByDate(Class<T> miCls, boolean ascending, int limit) {
		String dateField = createdTimestampField(miCls);
		DBObject orderBy = ascending ? DBSort.asc(dateField) : DBSort.desc(dateField);
		DBCursor<T> mpcn = getDBCollection(miCls).find().sort(orderBy);
		long timeout = 10000; //TODO: add configuration parameter for sorting collection by date
		return execute(mpcn, limit, timeout);
	}
	
	private <T extends XLiMeResource> String createdTimestampField(Class<T> miCls) {
		if (miCls.isAssignableFrom(MicroPostBean.class)) {
			return "created.timestamp";
		} else if (miCls.isAssignableFrom(NewsArticleBean.class)) {
			return "created.timestamp";
		} else if (miCls.isAssignableFrom(TVProgramBean.class)) {
			return "broadcastDate.timestamp";
		} else if (miCls.isAssignableFrom(ASRAnnotation.class)) {
			return "inSegment.startTime.timestamp";
		} else if (miCls.isAssignableFrom(OCRAnnotation.class)) {
			return "inSegment.startTime.timestamp";
		} else if (miCls.isAssignableFrom(SubtitleSegment.class)) {
			return "partOf.startTime.timestamp";
		} else if (miCls.isAssignableFrom(EntityAnnotation.class)) {
			return "insertionDate";
		} else throw new IllegalArgumentException("miCls: " + miCls);
	}
	
	private <T> Callable<List<T>> callableExecute(final DBCursor<T> cursor, final int limit) {
		return new Callable<List<T>>() {

			@Override
			public List<T> call() throws Exception {
				long start = System.currentTimeMillis();
				List<T> result = cursor.limit(limit).toArray(limit);
				log.debug(String.format("QueryExecution of %s on %s in %s ms.", 
						cursor.getQuery(), cursor.getCollection().getName(), (System.currentTimeMillis() - start)));
				return result;
			}
		};		
	}
	
}
