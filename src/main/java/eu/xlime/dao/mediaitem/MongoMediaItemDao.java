package eu.xlime.dao.mediaitem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang3.NotImplementedException;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.UIDate;
import eu.xlime.bean.VideoSegment;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.dao.MediaItemDao;
import eu.xlime.dao.MongoXLiMeResourceStorer;
import eu.xlime.dao.XLiMeResourceStorer;
import eu.xlime.datasum.bean.DatasetSummary;
import eu.xlime.mongo.DBCollectionProvider;
import eu.xlime.util.ListUtil;
import eu.xlime.util.score.ScoredSet;
import eu.xlime.util.score.ScoredSetImpl;

/**
 * Implements the {@link MediaItemDao} against an xLiMe mongoDB back-end.
 * 
 * @author rdenaux
 *
 */
public class MongoMediaItemDao extends AbstractMediaItemDao implements XLiMeResourceStorer {
	
	private static final Logger log = LoggerFactory.getLogger(MongoMediaItemDao.class);
	
	private final MongoXLiMeResourceStorer mongoStorer;
	private final DBCollectionProvider collectionProvider;
	private final static boolean ascending = true;
	
	public MongoMediaItemDao(Properties props) {
		collectionProvider = new DBCollectionProvider(props);
		mongoStorer = new MongoXLiMeResourceStorer(collectionProvider);
	}
	
	@Override
	public <T extends XLiMeResource> String insertOrUpdate(T mediaItem) {
		return mongoStorer.insertOrUpdate(mediaItem);
	}
	
	@Override
	public <T extends XLiMeResource> String insertOrUpdate(T xLiMeRes,
			Optional<Locale> optLocale) {
		return mongoStorer.insertOrUpdate(xLiMeRes, optLocale);
	}

	@Override
	public <T extends XLiMeResource> long count(Class<T> xLiMeResClass) {
		return mongoStorer.count(xLiMeResClass);
	}
	
	@Override
	public <T extends XLiMeResource> long count(Class<T> xLiMeResClass,
			Optional<Locale> optLocale) {
		return mongoStorer.count(xLiMeResClass, optLocale);
	}

	@Override
	public <T extends XLiMeResource> Optional<T> findResource(
			Class<T> resourceClass, String resourceUrl) {
		return mongoStorer.findResource(resourceClass, resourceUrl);
	}
	
	@Override
	public <T extends XLiMeResource> Optional<T> findResource(
			Class<T> resourceClass, Optional<Locale> optLocale,
			String resourceUrl) {
		return mongoStorer.findResource(resourceClass, optLocale, resourceUrl);
	}

	@Override
	public List<NewsArticleBean> findNewsArticles(List<String> uris) {
		return mongoStorer.getDBCollection(NewsArticleBean.class).find().in("_id", uris).toArray();
	}

	@Override
	public List<MicroPostBean> findMicroPosts(List<String> uris) {
		return mongoStorer.getDBCollection(MicroPostBean.class).find().in("_id", uris).toArray();
	}

	@Override
	public List<MicroPostBean> findMicroPostsByKeywordsFilter(
			List<String> keywordFilters) {
		log.warn(String.format("%s does not support searching by keywords filter", getClass().getSimpleName()));
		return ImmutableList.of();
	}

	@Override
	public List<TVProgramBean> findTVPrograms(List<String> uris) {
		return ImmutableList.copyOf(cleanMediaItems(mongoStorer.getDBCollection(TVProgramBean.class).find().in("_id", uris).toArray()));
	}

	@Override
	public ScoredSet<String> findMediaItemUrlsByText(String text) {
		DBObject textQ = new BasicDBObject(
			    "$text", new BasicDBObject("$search", text)
				);
		DBObject projection = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				);
		DBObject sorting = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				); 
		long start = System.currentTimeMillis();
		DBCursor<TVProgramBean> tvc = mongoStorer.getDBCollection(TVProgramBean.class).find(textQ, projection).sort(sorting).sort(DBSort.desc("broadcastDate.timestamp"));
		DBCursor<NewsArticleBean> nec = mongoStorer.getDBCollection(NewsArticleBean.class).find(textQ, projection).sort(sorting).sort(DBSort.desc("created.timestamp"));
		DBCursor<MicroPostBean> mpc = mongoStorer.getDBCollection(MicroPostBean.class).find(textQ, projection).sort(sorting).sort(DBSort.desc("created.timestamp"));
		log.debug("Created cursors in " + (System.currentTimeMillis() - start) + "ms.");
		return ScoredSetImpl.<String>builder()
			.addAll(toMediaItemUrlScoredSet(tvc, 10))
			.addAll(toMediaItemUrlScoredSet(nec, 10))
			.addAll(toMediaItemUrlScoredSet(mpc, 10))
			.build();
	}

	private <T extends MediaItem> ScoredSet<String> toMediaItemUrlScoredSet(
			DBCursor<T> cursor, int limit) {
		ScoredSet<T> mits = mongoStorer.toScoredSet(cursor, limit, "Found via text search", "score");
		ScoredSet.Builder<String> builder = ScoredSetImpl.builder();
		for (T mit: mits.unscored()) {
			builder.add(mit.getUrl(), mits.getScore(mit));
		}
		return builder.build();
	}	
		
	@Override
	public Optional<VideoSegment> findVideoSegment(String uri) {
		throw new NotImplementedException("");
	}

	@Override
	public List<String> findMediaItemsByDate(long dateFrom, long dateTo,
			int limit) {
		List<String> result = new ListUtil().weave(
				findTVProgramUrlsByDate(dateFrom, dateTo, limit/2),
				findNewsArticleUrlsByDate(dateFrom, dateTo, limit/2),
				findMicroPostUrlsByDate(dateFrom, dateTo, limit/2));
		if (result.size() > limit) 
			return ImmutableList.copyOf(result.subList(0, limit));
		else return result;
	}

	@Override
	public List<String> findMostRecentMediaItemUrls(int nMinutes, int limit) {
		List<String> result = new ListUtil().weave(
				findMostRecentTVProgramUrls(limit/2),
				findMostRecentNewsArticleUrls(limit/2),
				findMostRecentMicroPostUrls(limit/2));
		if (result.size() > limit) 
			return ImmutableList.copyOf(result.subList(0, limit));
		else return result;
	}
	
	private List<? extends String> findMostRecentMicroPostUrls(int limit) {
		return mapUrl(mongoStorer.getSortedByDate(MicroPostBean.class, !ascending, limit));
	}

	private List<? extends String> findMostRecentNewsArticleUrls(int limit) {
		return mapUrl(mongoStorer.getSortedByDate(NewsArticleBean.class, !ascending, limit));
	}

	private List<? extends String> findMostRecentTVProgramUrls(int limit) {
		return mapUrl(mongoStorer.getSortedByDate(TVProgramBean.class, !ascending, limit));
	}

	@Override
	public List<String> findAllMediaItemUrls(int limit) {
		throw new NotImplementedException("");
	}
	
	@Override
	public boolean hasMediaItemsAfter(long timestampFrom) {
		DatasetSummary sum = dsSummaFactory.createXLiMeMongoSummary();
		if (sum == null) return true;
		UIDate mpd = sum.getNewestMicropostDate();
		UIDate nad = sum.getNewestNewsarticleDate();
		UIDate tvd = sum.getNewestMediaresourceDate();
		final boolean hasMicroPostAfter = mpd != null && mpd.timestamp.getTime() > timestampFrom;
		final boolean hasNewsAfter = nad != null && mpd.timestamp.getTime() > timestampFrom;
		final boolean hasTVAfter = tvd != null && tvd.timestamp.getTime() > timestampFrom;
		log.debug(String.format("hasMicroPostAfter %s: %s", timestampFrom, hasMicroPostAfter));
		log.debug(String.format("hasNewsAfter %s: %s", timestampFrom, hasNewsAfter));
		log.debug(String.format("hasTVAfter %s: %s", timestampFrom, hasTVAfter));
		return (hasMicroPostAfter) ||
				(hasNewsAfter) ||
				(hasTVAfter);
	}

	List<TVProgramBean> findTVProgramsByDate(long dateFrom, long dateTo, int limit) {
		return findMediaItemByDate(TVProgramBean.class, "broadcastDate.timestamp", dateFrom, dateTo, limit);
	}

	List<String> findTVProgramUrlsByDate(long dateFrom, long dateTo, int limit) {
		return findMediaItemUrlsByDate(TVProgramBean.class, "broadcastDate.timestamp", dateFrom, dateTo, limit);
	}

	List<String> findNewsArticleUrlsByDate(long dateFrom, long dateTo, int limit) {
		return findMediaItemUrlsByDate(NewsArticleBean.class, "created.timestamp", dateFrom, dateTo, limit);
	}
	
	List<NewsArticleBean> findNewsArticlesByDate(long dateFrom, long dateTo, int limit) {
		return findMediaItemByDate(NewsArticleBean.class, "created.timestamp", dateFrom, dateTo, limit);
	}

	List<MicroPostBean> findMicroPostsByDate(long dateFrom, long dateTo,
			int limit){
		return findMediaItemByDate(MicroPostBean.class, "created.timestamp", dateFrom, dateTo, limit);
	}

	List<String> findMicroPostUrlsByDate(long dateFrom, long dateTo,
			int limit){
		return findMediaItemUrlsByDate(MicroPostBean.class, "created.timestamp", dateFrom, dateTo, limit);
	}
	
	<T extends MediaItem> List<T> findMediaItemByDate(Class<T> clazz, String timeStampBeanPath, long dateFrom, long dateTo, int limit) {
		return findMediaItemByDate(clazz, timeStampBeanPath, dateFrom, dateTo, limit, Optional.<DBObject>absent());
	}

	<T extends MediaItem> List<T> findMediaItemByDate(Class<T> clazz, String timeStampBeanPath, long dateFrom, long dateTo, int limit, Optional<DBObject> optProj) {
		if (log.isDebugEnabled())
			log.debug(String.format("Querying MongoDB for %ss between %s and %s", clazz.getSimpleName(), dateFrom, dateTo));
		JacksonDBCollection<T, String> coll = mongoStorer.getDBCollection(clazz);
		long start = System.currentTimeMillis();
		Query q = DBQuery.lessThan(timeStampBeanPath, new Date(dateTo)).greaterThanEquals(timeStampBeanPath, new Date(dateFrom));
		DBCursor<T> cursor = optProj.isPresent() ? coll.find(q, optProj.get()) : coll.find(q);
		List<T> result = cursor.limit(limit).toArray();
		if (log.isTraceEnabled())
			log.trace(String.format("Executed query in %s ms. and found %s results.", (System.currentTimeMillis() - start), cursor.count()));
		return result;
	}

	
	<T extends MediaItem> List<String> findMediaItemUrlsByDate(Class<T> clazz, String timeStampBeanPath, long dateFrom, long dateTo, int limit) {
		List<T> mits = findMediaItemByDate(clazz,  timeStampBeanPath,  dateFrom, dateTo, limit,
				Optional.<DBObject>absent() // TODO: only return the "url" field... is resulting in some exception... Optional.<DBObject>of(DBProjection.include("_id")
				);
		return mapUrl(mits);
	}
	
	List<String> mapUrl(List<? extends MediaItem> medItems) {
		List<String> result = new ArrayList<String>();
		for (MediaItem mi: medItems) {
			result.add(mi.getUrl());
		}
		return result;
	}
	
	
}
