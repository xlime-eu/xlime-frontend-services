package eu.xlime;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isoco.kontology.access.OntologyManager;
import com.isoco.kontology.ontologies.dao.OntologyManagerImpl.UserPassword;
import com.isoco.kontology.ontologies.dao.SesameDAOFactory;

import eu.xlime.bean.Content;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.UrlLabel;

public class MediaItemDao {

	private static final Logger log = LoggerFactory.getLogger(MediaItemDao.class);
	
	private static OntologyManager ontoManager;

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
	
	public MicroPostBean findMicroPost(String url) {
		final OntologyManager ontoMan = getOntoMan(); //KPlatformInitializer.footballKT;
		final String encUrl = "<" + url + ">";
		String query = "PREFIX xlime: <http://xlime-project.org/vocab/> " + 
				"PREFIX dcterms: <http://purl.org/dc/terms/> " + 
				"PREFIX sioc: <http://rdfs.org/sioc/ns#> " + 

				"SELECT ?created ?lang ?publisher ?source ?sourceType ?content ?creator { " +  
				encUrl + " a <http://rdfs.org/sioc/ns#MicroPost>." + 
				encUrl + " dcterms:created ?created. " +
				encUrl + " dcterms:language ?lang. " + 
				encUrl + " dcterms:publisher ?publisher. " +
				encUrl + " dcterms:source ?source. " + 
				encUrl + " xlime:hasSourceType ?sourceType. " + 
				" OPTIONAL { " + 
				encUrl + " sioc:content ?content. " + 
				"} " + 
				encUrl + " sioc:has_creator ?creator. " + 
				"} LIMIT 30";
		Map<String, Map<String, String>> result = ontoMan.executeAdHocSPARQLQuery(query);
		
		return toMicroPost(result, url);
	}

	private OntologyManager getOntoMan() {
		if (ontoManager != null) return ontoManager;
		ontoManager =
				new SesameDAOFactory().createRemoteDAO("http://km.aifb.kit.edu/services/xlime-sparql",
						//TODO: retrieve credentials from config file
						new UserPassword("xlime", "Iph&aen9tahsh6aej}ah9ie"), 2.0);
		return ontoManager;
	}
	
	private MicroPostBean toMicroPost(Map<String, Map<String, String>> resultSet,
			String url) {
		if (resultSet == null || resultSet.keySet().isEmpty()) throw new RuntimeException("No microPost with " + url);
		MicroPostBean result = new MicroPostBean();
		result.setUrl(url);
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			if (tuple.containsKey("created")) {
				String created = tuple.get("created");
				result.setCreated(extractISODate(created));
			}
			if (tuple.containsKey("lang")) {
				result.setLang(tuple.get("lang"));
			}
			if (tuple.containsKey("publisher")) {
				result.setPublisher(readPublisher(tuple.get("publisher")));
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
				result.setCreator(readCreator(tuple.get("creator")));
			}
		}
		
		return result;
	}
	
	private UrlLabel readCreator(String creatorUrl) {
		UrlLabel result = new UrlLabel();
		result.setUrl(creatorUrl);
		result.setLabel(creatorUrlToLabel(creatorUrl));
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
		UrlLabel result = new UrlLabel();
		result.setUrl(pubUrl);
		result.setLabel(pubUrlToLabel(pubUrl));
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
			log.warn("Found invalid ISODate " + dateTime, e);
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
