package eu.xlime.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.util.CacheFactory;

/**
 * Default implementation for retrieving {@link MediaItem} beans.
 * 
 * Its implementation aims to use the best combination of caching and querying available 
 * back-end data stores to provide "fresh" {@link MediaItem}s while providing quick responses.    
 * 
 * @author RDENAUX
 *
 */
public class MediaItemDaoImpl extends SparqlMediaItemDao {

	private static final Logger log = LoggerFactory.getLogger(MediaItemDaoImpl.class);
	
	
	private static Cache<String, NewsArticleBean> newsCache = CacheFactory.instance.buildCache("newsCache");
	private static Cache<String, MicroPostBean> microPostCache = CacheFactory.instance.buildCache("microPostCache");
	private static Cache<String, TVProgramBean> tvCache = CacheFactory.instance.buildCache("tvCache");
//	private static Cache<String, String> microPostPublisherLabelCache = CacheFactory.instance.buildCache("microPostPublisherLabelCache");	
//	private static Cache<String, String> microPostCreatorLabelCache = CacheFactory.instance.buildCache("microPostCreatorLabelCache");		

	/**
	 * Use the configured main xLIMeSparqlClient
	 */
	@Override
	protected SparqlClient getXLiMeSparqlClient() {
		return new SparqlClientFactory().getXliMeSparqlClient();
	}

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemDao#findNewsArticles(java.util.List)
	 */
	@Override
	public List<NewsArticleBean> findNewsArticles(List<String> uris) {
		Map<String, NewsArticleBean> cached = newsCache.getAllPresent(uris);
		List<String> toFind = new ArrayList(uris);
		toFind.removeAll(cached.keySet());
		
		Map<String, NewsArticleBean> nonCached = doFindNewsArticles(toFind);
		newsCache.putAll(nonCached);
		
		log.debug(String.format("Found %s cached and %s uncached newsArticles", cached.size(), nonCached.size()));
		
		return ImmutableList.<NewsArticleBean>builder()
				.addAll(cleanCached(cached.values())).addAll(nonCached.values())
				.build();
	}

	private <T extends MediaItem> Iterable<T> cleanCached(
			Collection<T> values) {
		List<T> result = new ArrayList<T>();
		for (T val: values) {
			result.add(cleanCached(val));
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends MediaItem> T cleanCached(T dirty) {
		if (dirty instanceof NewsArticleBean) return (T) clean((NewsArticleBean)dirty);
		if (dirty instanceof TVProgramBean) return (T) clean((TVProgramBean) dirty);
		if (dirty instanceof MicroPostBean) return (T) clean((MicroPostBean) dirty);
		log.warn("Unsupported mediaItem " + dirty);
		return dirty;
	}
	

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemDao#findMicroPosts(java.util.List)
	 */
	@Override
	public List<MicroPostBean> findMicroPosts(List<String> uris) {
		Map<String, MicroPostBean> cached = microPostCache.getAllPresent(uris);
		List<String> toFind = new ArrayList(uris);
		toFind.removeAll(cached.keySet());
		
		Map<String, MicroPostBean> nonCached = doFindMicroPosts(toFind);
		microPostCache.putAll(nonCached);
		
		log.debug(String.format("Found %s cached and %s uncached microPosts", cached.size(), nonCached.size()));
		
		return ImmutableList.<MicroPostBean>builder()
				.addAll(cleanCached(cached.values())).addAll(nonCached.values())
				.build();
	}
		

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemDao#findTVPrograms(java.util.List)
	 */
	@Override
	public List<TVProgramBean> findTVPrograms(List<String> uris) {
		Map<String, TVProgramBean> cached = tvCache.getAllPresent(uris);
		List<String> toFind = new ArrayList(uris);
		toFind.removeAll(cached.keySet());
		
		Map<String, TVProgramBean> nonCached = doFindTVPrograms(toFind);
		tvCache.putAll(nonCached);
		
		log.debug(String.format("Found %s cached and %s uncached tvPrograms", cached.size(), nonCached.size()));
		
		return ImmutableList.<TVProgramBean>builder()
				.addAll(cleanCached(cached.values()))
				.addAll(nonCached.values()) //tv programs also need cleaning 
				.build();
	}


	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemDao#findVideoSegment(java.lang.String)
	 */
	@Override
	public Optional<VideoSegment> findVideoSegment(String uri) {
		// TODO implement
		return Optional.absent();
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

	
}
