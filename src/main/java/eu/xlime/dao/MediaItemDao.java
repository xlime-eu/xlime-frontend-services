package eu.xlime.dao;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import eu.xlime.bean.Content;
import eu.xlime.bean.Duration;
import eu.xlime.bean.GeoLocation;
import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.UIDate;
import eu.xlime.bean.UrlLabel;
import eu.xlime.bean.VideoSegment;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.sparql.SparqlQueryFactory;
import eu.xlime.util.CacheFactory;
import eu.xlime.util.ResourceTypeResolver;

/**
 * Provides methods for retrieving {@link MediaItem} beans.
 * 
 * @author RDENAUX
 *
 */
public class MediaItemDao {

	private static final Logger log = LoggerFactory.getLogger(MediaItemDao.class);
	
	/**
	 * The 'ideal' maximum number of characters to allow in a Content preview.
	 * This limit is 'soft' in the sense that  
	 */
	private static int PreviewSoftMaxLength = 200;
	
	/**
	 * Percentage of the {@link #PreviewSoftMaxLength} by which a content preview 
	 * can exceed the {@link #PreviewSoftMaxLength}. 
	 */
	private static double PreviewMaxLengthAllowance = 0.1;
	
	private static final SparqlQueryFactory qFactory = new SparqlQueryFactory();
	private static final ResourceTypeResolver typeResolver = new ResourceTypeResolver();
	
	private static Cache<String, NewsArticleBean> newsCache = CacheFactory.instance.buildCache("newsCache");
	private static Cache<String, MicroPostBean> microPostCache = CacheFactory.instance.buildCache("microPostCache");
	private static Cache<String, TVProgramBean> tvCache = CacheFactory.instance.buildCache("tvCache");
	
	public Optional<? extends MediaItem> findMediaItem(final String url) {
		if (typeResolver.isNewsArticle(url))
			return findNewsArticle(url);
		else if (typeResolver.isMicroPost(url))
			return findMicroPost(url);
		else if (typeResolver.isTVProgram(url))
			return findTVProgram(url);
		else throw new RuntimeException("Cannot map url to a known xLiMe media-item type " + url);
	}
	
	public Optional<NewsArticleBean> findNewsArticle(final String url) {
		Callable<? extends NewsArticleBean> valueLoader = new Callable<NewsArticleBean>() {

			@Override
			public NewsArticleBean call() throws Exception {
				final SparqlClient sparqler = getXLiMeSparqlClient();
				String query = qFactory.newsArticleDetails(url);
				Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
				
				return toNewsArticle(result, url).get();
			}
			
		};
		try {
			return Optional.of(clean(newsCache.get(url, valueLoader)));
		} catch (ExecutionException e) {
			log.warn("Error loading value for " + url, e);
			return Optional.absent();
		}
	}
	
	private NewsArticleBean clean(NewsArticleBean newsArticleBean) {
		newsArticleBean.getCreated().resetTimeAgo();
		return newsArticleBean;
	}

	private MicroPostBean clean(MicroPostBean microPostBean) {
		microPostBean.getCreated().resetTimeAgo();
		return microPostBean;
	}

	public Optional<MicroPostBean> findMicroPost(final String url) {
		Callable<? extends MicroPostBean> valueLoader = new Callable<MicroPostBean>() {

			@Override
			public MicroPostBean call() throws Exception {
				final SparqlClient sparqler = getXLiMeSparqlClient();
				String query = qFactory.microPostDetails(url);
				Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);

				return toMicroPost(result, url).get();
			}
		};
		
		try {
			return Optional.of(clean(microPostCache.get(url, valueLoader)));
		} catch (ExecutionException e) {
			log.warn("Error loading value for " + url, e);
			return Optional.absent();
		}
	}

	public Optional<TVProgramBean> findTVProgram(final String url) {
		Callable<? extends TVProgramBean> valueLoader = new Callable<TVProgramBean>() {

			@Override
			public TVProgramBean call() throws Exception {
				final SparqlClient sparqler = getXLiMeSparqlClient();
				String query = qFactory.mediaResource(url);
				log.debug("Retrieving tv program with: " + query);
				Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
//						mockMediaResourceResult(url);

				return toTVProgramBean(result, url).get();
			}
		};		
		
		try {
			return Optional.of(clean(tvCache.get(url, valueLoader)));
		} catch (ExecutionException e) {
			log.warn("Error loading value for " + url, e);
			return Optional.absent();
		}		
	}

	private TVProgramBean clean(TVProgramBean tvProgramBean) {
		tvProgramBean.getBroadcastDate().resetTimeAgo();
		if (tvProgramBean.getRelatedImage() != null &&
				tvProgramBean.getRelatedImage().startsWith("http://cms-staging.zattoo.com")) {
			log.debug("Filter out sandbox zattoo related images as these are forbidden by the server.");
			tvProgramBean.setRelatedImage(null);
		}
		return tvProgramBean;
	}

	public Optional<VideoSegment> findVideoSegment(String uri) {
		// TODO implement
		return Optional.absent();
	}
	
	/**
	 * Returns a list of recent media item urls, where recent means in the last <code>nMinutes</code>.
	 *  
	 * @param nMinutes a positive number of minutes. Used to retrieve the list of 'recent' mediaItems.
	 * @param limit a positive number which imposes a hard-coded limit on the number of results to return.
	 *  
	 * @return
	 */
	public List<String> findLatestMediaItemUrls(int nMinutes, int limit) {
		final SparqlClient sparqler = getXLiMeSparqlClient();
		Date now = new Date();
		DateTimeFormatter formatter1 = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
		DateTimeFormatter formatter2 = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss");
		long dateFrom = now.getTime() - (nMinutes * 60 * 1000);
		long dateTo = now.getTime();
		String query = qFactory.mediaItemUrlsByDate(dateFrom, dateTo, limit, formatter1, formatter2);
		log.trace("Retrieving latest media items using: " + query);
		Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
		log.debug(String.format("Found %s media items between %s and %s", result.size(), now, "" + nMinutes + " ago"));
		return toUrlList(result);
	}
	
	private List<String> toUrlList(Map<String, Map<String, String>> resultSet) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("Empty resultset ");
			return ImmutableList.of();
		}
		List<String> result = new ArrayList<>();
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			result.add(tuple.get("s"));
		}
		return result;
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

	private Optional<TVProgramBean> toTVProgramBean(
			Map<String, Map<String, String>> resultSet, String url) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("No tv programme with " + url);
			return Optional.absent();
		}
		TVProgramBean result = new TVProgramBean();
		result.setUrl(url);
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			if (tuple.containsKey("broadcastDate")) {
				String created = tuple.get("broadcastDate");
				result.setBroadcastDate(asUIDate(extractISODate(created)));
			}
			if (tuple.containsKey("title")) {
				result.setTitle(tuple.get("title"));
			}
			if (tuple.containsKey("description")) {
				String content = tuple.get("description");
				if (content != null) result.setDescription(readContent(content));
				else result.setDescription(noDescriptionContent());
			} else result.setDescription(noDescriptionContent());
			if (tuple.containsKey("duration")) {
				result.setDuration(readDuration(tuple.get("duration")));
			}
			if (tuple.containsKey("publisher")) {
				result.setPublisher(readTVPublisher(tuple.get("publisher")));
			}
			if (tuple.containsKey("relImage")) {
				result.setRelatedImage(tuple.get("relImage"));
			}
			if (tuple.containsKey("source")) {
				result.setSource(readTVSource(tuple));
			}
			Optional<GeoLocation> optGeoLoc = toTVGeoLocation(tuple);
			if (optGeoLoc.isPresent()) {
				result.setRelatedLocation(optGeoLoc.get());
			}
		}
		
		return Optional.of(result);
	}

	private Content noDescriptionContent() {
		Content result = new Content();
		result.setFull("No description available");
		return result;
	}

	private Duration readDuration(String string) {
		Double d = Double.valueOf(string);
		return new Duration(d);
	}

	private Optional<GeoLocation> toTVGeoLocation(Map<String, String> tuple) {
		if (!tuple.containsKey("geoname")) return Optional.absent();
		GeoLocation result = new GeoLocation();
		result.setLabel(tuple.get("geoname"));
		//TODO: can we expand zattoo's location label into a proper geoname?
		return Optional.of(result);
	}

	private UrlLabel readTVSource(Map<String, String> tuple) {
		UrlLabel result = new UrlLabel();
		result.setLabel(tuple.get("publisher"));
		result.setUrl(tuple.get("source"));
		return result;
	}

	private SparqlClient getXLiMeSparqlClient() {
		return new SparqlClientFactory().getXliMeSparqlClient();
	}
	
	private Optional<NewsArticleBean> toNewsArticle(Map<String, Map<String, String>> resultSet,
			String url) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("No newsArticle with " + url);
			return Optional.absent();
		}
		NewsArticleBean result = new NewsArticleBean();
		result.setUrl(url);
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			if (tuple.containsKey("created")) {
				String created = tuple.get("created");
				result.setCreated(asUIDate(extractISODate(created)));
			}
			if (tuple.containsKey("lang")) {
				result.setLang(tuple.get("lang"));
			}
			if (tuple.containsKey("publisher")) {
				result.setPublisher(readPublisher(tuple.get("publisher")));
			} else if (tuple.containsKey("source")) {
				result.setPublisher(readPublisherFromSource(tuple.get("source")));
			}
			if (tuple.containsKey("source")) {
				result.setSource(tuple.get("source"));
			}
			if (tuple.containsKey("title")) {
				result.setTitle(tuple.get("title"));
			}
			if (tuple.containsKey("content")) {
				String content = tuple.get("content");
				if (content != null) result.setContent(readContent(content));
			}
			Optional<GeoLocation> optGeoLoc = toGeoLocation(tuple);
			if (optGeoLoc.isPresent()) {
				result.setLocation(optGeoLoc.get());
			}
		}
		
		return Optional.of(result);
	}
	
	private UrlLabel readPublisherFromSource(String sourceUrl) {
		try {
			URI uri = URI.create(sourceUrl);
			UrlLabel result = new UrlLabel();
			String host = uri.getHost();
			if (host == null) return null;
			result.setLabel(host);
			result.setUrl(host);
			return result;
		} catch (Exception e) {
			log.warn("Failed to extract publisher from sourceUrl" + sourceUrl);
			return null;
		}
	}

	private Optional<GeoLocation> toGeoLocation(Map<String, String> tuple) {
		if (tuple == null || tuple.keySet().isEmpty()) return Optional.absent();
		if (!tuple.keySet().containsAll(ImmutableSet.of("geolat", "geolong"))) {
			log.warn("Cannot extract geolocation from tuple due to mising 'geolat' or 'geolong' properties. Available properties: " + tuple.keySet());
			if (log.isTraceEnabled()) {
				log.trace("Original tuple " + tuple);
			}
			return Optional.absent();
		}
		GeoLocation result = new GeoLocation();
		if (tuple.containsKey("geolat")) {
			String geolat = tuple.get("geolat");
			result.setLat(asFloat(geolat));
		}
		if (tuple.containsKey("geolon")) {
			result.setLon(asFloat(tuple.get("geolon")));
		}
		if (tuple.containsKey("geoname")) {
			result.setLabel(tuple.get("geoname"));
		}
		return Optional.of(result);
	}
	
	private Optional<MicroPostBean> toMicroPost(Map<String, Map<String, String>> resultSet,
			String url) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("No microPost with " + url);
			return Optional.absent();
		}
		MicroPostBean result = new MicroPostBean();
		result.setUrl(url);
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			if (tuple.containsKey("created")) {
				String created = tuple.get("created");
				result.setCreated(asUIDate(extractISODate(created)));
			}
			if (tuple.containsKey("lang")) {
				result.setLang(tuple.get("lang"));
			}
			if (tuple.containsKey("publisher")) {
				result.setPublisher(readPublisher(tuple.get("publisher"), getOpt(tuple, "pubName")));
			}
			if (tuple.containsKey("source")) {
				result.setSource(tuple.get("source"));
			}
			if (tuple.containsKey("sourceType")) {
				result.setSourceType(tuple.get("sourceType"));
			}
			if (tuple.containsKey("content")) {
				String content = tuple.get("content");
				if (content != null) result.setContent(readContent(content));
			}
			if (tuple.containsKey("creator")) {
				result.setCreator(readCreator(tuple.get("creator"), getOpt(tuple, "creatorLabel")));
			}
		}
		
		return Optional.of(result);
	}
	
	private Optional<String> getOpt(Map<String, String> tuple, String key) {
		if (tuple.containsKey(key)) {
			return Optional.fromNullable(tuple.get(key));
		} else return Optional.absent();
	}

	private UIDate asUIDate(Date aDate) {
		return new UIDate(aDate);
	}

	private float asFloat(String val) {
		return Float.valueOf(val);
	}
	
	private UrlLabel readCreator(String creatorUrl, Optional<String> optLabel) {
		UrlLabel result = new UrlLabel();
		result.setUrl(creatorUrl);
		String label = optLabel.isPresent() ? optLabel.get() : creatorUrlToLabel(creatorUrl); 
		result.setLabel(label);
		return result;
	}

	/**
	 * Coins a label for a given creatorUrl. E.g.
	 * <code>
	 * creatorUrlToLabel("http://clubsearay.com/forum/index.php#Chris%20Nowell") // returns "Chris Nowell"
	 * creatorUrlToLabel("http://twitter.com/YummyDestiny") // returns "YummyDestiny"
	 * creatorUrlToLabel("https://plus.google.com/117956042043423124419") //returns ??
	 * creatorUrlToLabel("http://jjjameson65.wordpress.com#jjjameson65") //returns "jjjameson65"
	 * creatorUrlToLabel("https://www.facebook.com#Daniel%20Cosenza") //return "Daniel Cosenza"
	 * </code>
	 * @param creatorUrl
	 * @return
	 */
	final String creatorUrlToLabel(String creatorUrl) {
		if (creatorUrl.startsWith("http://twitter.com/"))
			return tryDecode(creatorUrl.substring("http://twitter.com/".length()));
		if (creatorUrl.startsWith("http://www.facebook.com#"))
			return tryDecode(creatorUrl.substring("http://www.facebook.com#".length()));
		if (creatorUrl.startsWith("https://plus.google.com/"))
			return "Google+ user"; //TODO: use g+ api to retrieve user info?
		String escapedFragment = extractFragment(creatorUrl);
		if (escapedFragment != null) return escapedFragment;
		return "creator"; //the default label
	}

	private String extractFragment(String aUrl) {
		int hashIndex = aUrl.lastIndexOf('#');
		if (hashIndex < 0) return null;
		String fragment = aUrl.substring(hashIndex + 1);
		return tryDecode(fragment);
	}

	private String tryDecode(String str) {
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}

	private Content readContent(String content) {
		Content result = new Content();
		result.setFull(content);
		result.setPreview(asPreview(content));
		return result;
	}

	private String asPreview(String fullContent) {
		int hardMax = PreviewSoftMaxLength + (int)(PreviewSoftMaxLength * PreviewMaxLengthAllowance);
		if (fullContent.length() < hardMax)
			return null; // fullContent doesn't need a preview since it's short enough
		String preview = fullContent.substring(0, PreviewSoftMaxLength) + "...";
		return preview;
	}

	private UrlLabel readPublisher(String pubUrl) {
		return readPublisher(pubUrl, Optional.<String>absent());
	}
	
	private UrlLabel readPublisher(String pubUrl, Optional<String> optLabel) {
		UrlLabel result = new UrlLabel();
		result.setUrl(pubUrl);
		if (optLabel.isPresent())
			result.setLabel(optLabel.get());
		else result.setLabel(pubUrlToLabel(pubUrl));
		return result;
	}

	private UrlLabel readTVPublisher(String pubLabel) {
		UrlLabel result = new UrlLabel();
//		result.setUrl(); //unknown? can we map to a page on zattoo for the channel?
		result.setLabel(pubLabel);
		return result;
	}
	
	private String pubUrlToLabel(String pubUrl) {
		if (pubUrl.contains("/www.twitter.com")) return "Twitter";
		if (pubUrl.contains("/plus.google.com")) return "Google+";
		if (pubUrl.contains("/www.facebook.com")) return "Facebook";
		return extractMainDomain(pubUrl);
	}

	/**
	 * Extracts the 'main domain' from a full URL, e.g.:
	 * <code>
	 * extractMainDomain("http://www.twitter.com") //returns "twitter.com"
	 * extractMainDomain("http://jjjameson65.wordpress.com") //returns "wordpress.com"
	 * extractMainDomain("http://clubsearay.com/forum/index.php"); //returns "clubsearay.com"
	 * </code> 
	 * @param url
	 * @return
	 */
	final String extractMainDomain(String url) {
		int endProtocol = url.indexOf("://");
		if (endProtocol > 0) {
			endProtocol = endProtocol + "://".length();
			int endDomain = url.indexOf("/", endProtocol);
			if (endDomain < 0) endDomain = url.length();
			final String host = url.substring(endProtocol, endDomain);
			int topLevelDomSeparator = host.lastIndexOf('.');
			if (topLevelDomSeparator > 0) {
				String hostNoTLD = host.substring(0, topLevelDomSeparator);
				int mainLevelDomSep = hostNoTLD.lastIndexOf('.');
				if (mainLevelDomSep > 0) {
					return host.substring(mainLevelDomSep + 1, host.length());
				} else {
					return host;
				}
			} else { //no top level domain in host?
				return host;
			}
		} else {
			//failed, return whole url?
			return url;
		}
	}

	final Date extractISODate(String dateTime) {
		if (dateTime == null || dateTime.isEmpty()) return null;
		DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
		try {
			return parser.parseDateTime(dateTime).toDate();
		} catch (IllegalArgumentException e) {
			log.trace("Found invalid ISODate " + dateTime + " Attempting failback format..");
			return failbackDateNoTimezone(dateTime);
		}
	}
	
	private Date failbackDateNoTimezone(String dateTime) {
		SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY);
		try {
			return ISO8601DATEFORMAT.parse(dateTime);
		} catch (ParseException e) {
			throw new RuntimeException("Error parsing " + dateTime, e);
		}
	}

	
}
