package eu.xlime.dao;

import java.util.List;

import com.google.common.base.Optional;

import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;

/**
 * Interface for retrieving {@link MediaItem}s from some back-end storage 
 * (e.g. a triple-store or document store).
 * 
 * @author rdenaux
 *
 */
public interface MediaItemDao {

	Optional<? extends MediaItem> findMediaItem(String url);

	List<MediaItem> findMediaItems(List<String> urls);
	
	<T extends MediaItem> List<T> findMediaItems(Class<T> clazz, List<String> urls);

	List<NewsArticleBean> findNewsArticles(List<String> uris);

	Optional<NewsArticleBean> findNewsArticle(String url);

	List<MicroPostBean> findMicroPosts(List<String> uris);
	
	List<MicroPostBean> findMicroPostsByKeywordsFilter(List<String> keywordFilters);

	Optional<MicroPostBean> findMicroPost(String url);

	List<TVProgramBean> findTVPrograms(List<String> uris);

	Optional<TVProgramBean> findTVProgram(String url);

	Optional<VideoSegment> findVideoSegment(String uri);

	/**
	 * Returns a list of recent media item urls, where recent means in the last <code>nMinutes</code>.
	 *  
	 * @param nMinutes a positive number of minutes. Used to retrieve the list of 'recent' mediaItems.
	 * @param limit a positive number which imposes a hard-coded limit on the number of results to return.
	 *  
	 * @return
	 */
	List<String> findLatestMediaItemUrls(int nMinutes, int limit);

	/**
	 * Returns a list of all the media item urls available to this {@link MediaItemDao}. 
	 * Note that this may be a huge number, depending on the back-end being used. So,
	 * you should be careful when calling this method.
	 * 
	 * @param limit
	 * @return
	 */
	List<String> findAllMediaItemUrls(int limit);
}