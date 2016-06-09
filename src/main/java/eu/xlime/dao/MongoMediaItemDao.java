package eu.xlime.dao;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.NotImplementedException;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.mongodb.DuplicateKeyException;

import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.mongo.DBCollectionProvider;

public class MongoMediaItemDao extends AbstractMediaItemDao implements MediaItemStorer {
	
	private static final Logger log = LoggerFactory.getLogger(MongoMediaItemDao.class);
	
	private final DBCollectionProvider collectionProvider;
	
	public MongoMediaItemDao(Properties props) {
		collectionProvider = new DBCollectionProvider(props);
	}
	
	@Override
	public <T extends MediaItem> String insertOrUpdate(T mediaItem) {
		JacksonDBCollection<T, String> coll = getCollection(mediaItem);
		if (log.isDebugEnabled()) {
			log.debug(String.format("Collection %s (%s docs) before upserting %s", coll.getName(), coll.count(), mediaItem.getUrl()));
		}
		String id = null;
		String op = "inserting";
		try {
			WriteResult<T, String> result = coll.insert(mediaItem);
			id = result.getSavedId();
		} catch (DuplicateKeyException e) {
			//object already in, update instead
			WriteResult<T, String> result = coll.updateById(mediaItem.getUrl(), mediaItem);
			id = mediaItem.getUrl();
			op = "updating";
		}
		if (log.isDebugEnabled()) {
			log.debug(String.format("Collection %s (%s docs) after %s, saved id %s", coll.getName(), coll.count(), op, mediaItem.getUrl(), id));
		}
		return id;
	}
	
	public <T extends MediaItem> long count(Class<T> mediaItemClass) {
		JacksonDBCollection<T, String> coll = getDBCollection(mediaItemClass);
		return coll.count();
	}

	@Override
	public List<NewsArticleBean> findNewsArticles(List<String> uris) {
		return getDBCollection(NewsArticleBean.class).find().in("_id", uris).toArray();
	}

	@Override
	public List<MicroPostBean> findMicroPosts(List<String> uris) {
		return getDBCollection(MicroPostBean.class).find().in("_id", uris).toArray();
	}

	@Override
	public List<MicroPostBean> findMicroPostsByKeywordsFilter(
			List<String> keywordFilters) {
		throw new NotImplementedException("");
	}

	@Override
	public List<TVProgramBean> findTVPrograms(List<String> uris) {
		return getDBCollection(TVProgramBean.class).find().in("_id", uris).toArray();
	}

	@Override
	public Optional<VideoSegment> findVideoSegment(String uri) {
		throw new NotImplementedException("");
	}

	@Override
	protected List<String> findMediaItemsByDate(long dateFrom, long dateTo,
			int limit) {
		throw new NotImplementedException("");
	}

	@Override
	public List<String> findAllMediaItemUrls(int limit) {
		throw new NotImplementedException("");
	}

	List<MicroPostBean> findMicroPostsByDate(long dateFrom, long dateTo,
			int limit){
		if (log.isDebugEnabled())
			log.debug(String.format("Querying MongoDB for microposts between %s and %s", dateFrom, dateTo));
		JacksonDBCollection<MicroPostBean, String> coll = getDBCollection(MicroPostBean.class);
		long start = System.currentTimeMillis();
		Query q = DBQuery.lessThan("created.timestamp", new Date(dateTo)).greaterThanEquals("created.timestamp", new Date(dateFrom));
		DBCursor<MicroPostBean> cursor = coll.find(q);
		List<MicroPostBean> result = cursor.limit(limit).toArray();
		if (log.isTraceEnabled())
			log.trace(String.format("Executed query in %s ms. and found %s results.", (System.currentTimeMillis() - start), cursor.count()));
		return result;
	}
	
	private <T extends MediaItem> JacksonDBCollection<T, String> getCollection(
			T mediaItem) {
		Class<T> miClass = (Class<T>)mediaItem.getClass();
		return getDBCollection(miClass);
	}

	private <T extends MediaItem> JacksonDBCollection<T, String> getDBCollection(
			Class<T> miClass) {
		JacksonDBCollection<T, String> coll = JacksonDBCollection.wrap(collectionProvider.getDBCollection(miClass), miClass,
		        String.class);
		return coll;
	}
	
}
