package eu.xlime.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import eu.xlime.Config;
import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.dao.mediaitem.AbstractMediaItemDao;
import eu.xlime.dao.mediaitem.MongoMediaItemDao;
import eu.xlime.util.score.ScoredSet;

/**
 * Default implementation for retrieving {@link MediaItem} beans.
 * 
 * Its implementation aims to use the best combination of caching and querying available 
 * back-end data stores to provide "fresh" {@link MediaItem}s while providing quick responses.    
 * 
 * @author RDENAUX
 *
 */
public class MediaItemDaoImpl extends AbstractMediaItemDao {

	private static final Logger log = LoggerFactory.getLogger(MediaItemDaoImpl.class);
	
	private final MediaItemDao delegate;
	
	public MediaItemDaoImpl() {
//		delegate = new CachingMediaItemDao(new XLiMeSparqlMediaItemDao());
		delegate = new MongoMediaItemDao(new Config().getCfgProps()); //TODO: maybe use a combination of Mongo and Sparql?
	}

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemDao#findNewsArticles(java.util.List)
	 */
	@Override
	public List<NewsArticleBean> findNewsArticles(List<String> uris) {
		return delegate.findNewsArticles(uris);
	}

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemDao#findMicroPosts(java.util.List)
	 */
	@Override
	public List<MicroPostBean> findMicroPosts(List<String> uris) {
		return delegate.findMicroPosts(uris);
	}
		

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemDao#findTVPrograms(java.util.List)
	 */
	@Override
	public List<TVProgramBean> findTVPrograms(List<String> uris) {
		return delegate.findTVPrograms(uris);
	}

	@Override
	public ScoredSet<String> findMediaItemUrlsByText(String text) {
		return delegate.findMediaItemUrlsByText(text);
	}

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemDao#findVideoSegment(java.lang.String)
	 */
	@Override
	public Optional<VideoSegment> findVideoSegment(String uri) {
		return delegate.findVideoSegment(uri);
	}

	@Deprecated 
	private Map<String, Map<String, String>> mockMediaResourceResult(String url) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder(); 
		Map<String, String> result = builder
				.put("broadcastDate", "2016-03-16T21:30:00Z")
				.put("title", "BBC World News America")
				.put("description", "In-depth reports on the major international and US news of the day with Katty Kay.")
				.put("duration", "1800.0")
				.put("publisher", "BBC World News")
				.put("relImage", "http://images.zattic.com/system/images/fba8/c599/ebad/6852/c496/format_480x360.jpg")
				.put("source", "http://zattoo.com/program/bbc-world-service/111277860")
				.put("geoname", "GB")
				.build();
		return ImmutableMap.of("0", result);
	}

	@Override
	public List<MicroPostBean> findMicroPostsByKeywordsFilter(
			List<String> keywordFilters) {
		return delegate.findMicroPostsByKeywordsFilter(keywordFilters);
	}

	@Override
	public List<String> findAllMediaItemUrls(int limit) {
		return delegate.findAllMediaItemUrls(limit);
	}

	@Override
	public List<String> findMediaItemsByDate(long dateFrom, long dateTo,
			int limit) {
		return delegate.findMediaItemsByDate(dateFrom, dateTo, limit);
	}

	
}
