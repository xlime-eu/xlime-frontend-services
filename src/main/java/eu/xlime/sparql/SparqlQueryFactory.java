package eu.xlime.sparql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

public class SparqlQueryFactory {

	private static final Logger log = LoggerFactory.getLogger(SparqlQueryFactory.class);
	
	public String dbpediaUIEntity(String url, String lang) {
		final String encUrl = "<" + url + ">";
		return 	"PREFIX dbo: <http://dbpedia.org/ontology/> " +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				"SELECT DISTINCT ?label ?depiction ?type FROM <http://dbpedia.org> WHERE { " +
			    " OPTIONAL { " + 
				  encUrl + " a ?type . " +
			    " } " +
				
				" OPTIONAL { " + 
				encUrl + " <http://www.w3.org/2000/01/rdf-schema#label> ?label ." +
				  String.format("FILTER regex(lang(?label), \"%s\", \"i\") . ", lang) +
				" } " +
						
				" OPTIONAL { " +
				 "{ " + encUrl + " foaf:depiction ?depiction . } UNION " +
				 "{ " + encUrl + " dbo:thumbnail ?depiction . } " +
				" } " +
				 
			"}";
	}
	
	public String microPostEntityAnnotations(String url) {
		final String encUrl = "<" + url + ">";
		return "PREFIX xlime: <http://xlime-project.org/vocab/> " + 
			"PREFIX dcterms: <http://purl.org/dc/terms/> " + 
			"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +

			"SELECT DISTINCT ?mp ?ent ?confidence ?start ?end { " + 
			  encUrl + " a sioc:MicroPost. " +
			  encUrl + " xlime:hasAnnotation ?a. " +
			  "?a xlime:hasEntity ?ent. " +
			  "?a xlime:hasConfidence ?confidence. " +
			  "?a xlime:hasPosition ?pos. " +
			  "?pos xlime:hasStartPosition ?start. " +
			  "?pos xlime:hasStopPosition ?end. " +
		"}";
	}
	
	public String newsArticleEntityAnnotations(String url) {
		final String encUrl = "<" + url + ">";
		return "PREFIX xlime: <http://xlime-project.org/vocab/> " + 
			"PREFIX dcterms: <http://purl.org/dc/terms/> " + 
			"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +
			"PREFIX kdo: <http://kdo.render-project.eu/kdo#> " +
			
			"SELECT DISTINCT ?mp ?ent ?confidence ?entLabel { " + 
			  encUrl + " a kdo:NewsArticle. " +
			  encUrl + " xlime:hasAnnotation ?a. " +
			  "?a xlime:hasEntity ?ent. " +
			  "?a xlime:hasConfidence ?confidence. " +
			  " OPTIONAL { " +
			   "?a rdfs:label ?entLabel. " +
			  "}" +
		"}";
	}
	
	public String sameAs(String url) {
		final String encUrl = "<" + url + ">";
		return 	
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
			"SELECT DISTINCT ?sameAs { " + 
			  encUrl + " (owl:sameAs|^owl:sameAs){,2} ?sameAs. " + //using property paths
		"}";

	}
	
	public String dbpediaSameAsPrimaryTopicOf(String wikiUrl) {
		final String encUrl = "<" + wikiUrl + ">";
		return 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
			
			"SELECT DISTINCT ?dbp ?sameAs { " + 
			  "?dbp foaf:isPrimaryTopicOf " + encUrl + " . " +
			  "?dbp owl:sameAs ?dbpSameAs. " +
		"}";
	}
	
	public String microPostDetails(List<String> urls) {
		int limit = (30 * urls.size()) + 1;
		String qPattern = load("sparql/microPostDetails.rq");
		return qPattern.replaceAll("#ReplaceByFilter", filterOneOfUrls("?url", urls))
			.replaceAll("#ReplaceLimit", "LIMIT " + limit);
	}
	
	public String siocNameOf(String url) {
		int limit = 1;
		return "PREFIX sioc: <http://rdfs.org/sioc/ns#> " + 
			"SELECT ?label ?g { " +
			"GRAPH ?g {" + 
			bracketUrl(url) + 
			    " sioc:name ?label . " +
			"} #end graph \n" + 
			"} LIMIT " + limit;
	}

	public String labelOf(String url) {
		int limit = 1;
		return "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + 
			"SELECT ?label ?g{" +
			"GRAPH ?g { " + 
			bracketUrl(url) + " rdfs:label ?label. " +
			"} " +
			"} LIMIT " + limit;
	}
	
	public String microPostDetails(String url) {
		final String encUrl = "<" + url + ">";
		return "PREFIX xlime: <http://xlime-project.org/vocab/> " + 
				"PREFIX dcterms: <http://purl.org/dc/terms/> " + 
				"PREFIX sioc: <http://rdfs.org/sioc/ns#> " + 

				"SELECT ?created ?lang ?publisher ?pubName ?source ?sourceType ?content ?creator ?creatorLabel { " +  
				encUrl + " a <http://rdfs.org/sioc/ns#MicroPost>." + 
				encUrl + " dcterms:created ?created. " +
				encUrl + " dcterms:language ?lang. " + 
				encUrl + " dcterms:publisher ?publisher. " +
				" OPTIONAL { " +
				  "?publisher rdfs:label ?pubName. " + 
				" } " + 
				encUrl + " dcterms:source ?source. " + 
				encUrl + " xlime:hasSourceType ?sourceType. " + 
				" OPTIONAL { " + 
				encUrl + " sioc:content ?content. " + 
				"} " + 
				encUrl + " sioc:has_creator ?creator. " + 
				" OPTIONAL { " +
				  " ?creator sioc:name ?creatorLabel . " + 
				"} " +
				"} LIMIT 30";		
	}
	

	public String newsArticleDetails(List<String> urls) {
		int limit = (30 * urls.size()) + 1;
		return "PREFIX xlime: <http://xlime-project.org/vocab/> " +  
			"PREFIX dcterms: <http://purl.org/dc/terms/> " +  
			"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +  
			"PREFIX kdo: <http://kdo.render-project.eu/kdo#> " +
			"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> " + 

			"SELECT ?url ?created ?lang ?publisher ?source ?title ?content ?geolat ?geolon ?geoname { " + 
			"?url a kdo:NewsArticle. " + 
			"?url dcterms:created ?created. " +  
			"?url dcterms:language ?lang. " +
			" OPTIONAL { " +
				"?url dcterms:publisher ?pub. " +
				" ?pub rdfs:label ?publisher. " +
			"}" +
			"?url dcterms:source ?source. " + 
			"?url dcterms:title ?title . " +
			" OPTIONAL { " +
				"?url sioc:content ?content. } " +
			" OPTIONAL { " +
				"?url dcterms:spatial ?spat. " +
				"?spat geo:lat ?geolat. " +
				"?spat geo:long ?geolon " +
				"OPTIONAL { " + 
					"?spat <http://www.geonames.org/ontology#name> ?geoname" +
				"}" +
			"}" + 
			filterOneOfUrls("?url", urls) + 
			"} LIMIT " + limit;
	}
	
	public String newsArticleDetails(String url) {
		final String encUrl = "<" + url + ">";
		return "PREFIX xlime: <http://xlime-project.org/vocab/> " +  
			"PREFIX dcterms: <http://purl.org/dc/terms/> " +  
			"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +  
			"PREFIX kdo: <http://kdo.render-project.eu/kdo#> " +
			"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> " + 

			"SELECT ?created ?lang ?publisher ?source ?title ?content ?geolat ?geolon ?geoname { " + 
			encUrl + " a kdo:NewsArticle. " + 
			encUrl + " dcterms:created ?created. " +  
			encUrl + " dcterms:language ?lang. " +
			" OPTIONAL { " +
				encUrl + " dcterms:publisher ?pub. " +
				" ?pub rdfs:label ?publisher. " +
			"}" +
			encUrl + " dcterms:source ?source. " + 
			encUrl + " dcterms:title ?title . " +
			" OPTIONAL { " +
				encUrl + " sioc:content ?content. } " +
			" OPTIONAL { " +
				encUrl + " dcterms:spatial ?spat. " +
				"?spat geo:lat ?geolat. " +
				"?spat geo:long ?geolon " +
				"OPTIONAL { " + 
					"?spat <http://www.geonames.org/ontology#name> ?geoname" +
				"}" +
			"}" + 
			"} LIMIT 30";
	}

	public String mediaResource(List<String> urls) {
		int limit = (30 * urls.size()) + 1;
		return "PREFIX xlime: <http://xlime-project.org/vocab/> " +  
		"PREFIX dcterms: <http://purl.org/dc/terms/> " +  
		"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +  
		"PREFIX ma: <http://www.w3.org/ns/ma-ont#> " +
		"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> " + 

		"SELECT ?url ?broadcastDate ?duration ?publisher ?relImage ?source ?title ?description ?geoname { " + 
		"?url a ma:MediaResource. " + 
		"?url ma:date ?broadcastDate. " +
		"?url ma:duration ?duration." +
		"?url ma:title ?title . " +
		" OPTIONAL { " +
			"?url ma:hasPublisher ?pub. " +
			" ?pub rdfs:label ?publisher. " +
		"}" +
		" OPTIONAL { " + 
			"?url ma:hasSource ?source. " +
		"} " +
		" OPTIONAL { " + 
		    "?url ma:description ?description ." +
		"}" +
		" OPTIONAL { " +
			"?url ma:hasRelatedImage ?relImage. } " +
		" OPTIONAL { " +
			"?url ma:hasRelatedLocation ?geoname. " +
		"}" + 
		filterOneOfUrls("?url", urls) +
		"} LIMIT " + limit;
	}
	
	public String mediaResource(String url) {
		final String encUrl = "<" + url + ">";
		return "PREFIX xlime: <http://xlime-project.org/vocab/> " +  
			"PREFIX dcterms: <http://purl.org/dc/terms/> " +  
			"PREFIX sioc: <http://rdfs.org/sioc/ns#> " +  
			"PREFIX ma: <http://www.w3.org/ns/ma-ont#> " +
			"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> " + 

			"SELECT ?broadcastDate ?duration ?publisher ?relImage ?source ?title ?description ?geoname { " + 
			encUrl + " a ma:MediaResource. " + 
			encUrl + " ma:date ?broadcastDate. " +
			encUrl + " ma:duration ?duration." +
			encUrl + " ma:title ?title . " +
			" OPTIONAL { " +
				encUrl + " ma:hasPublisher ?pub. " +
				" ?pub rdfs:label ?publisher. " +
			"}" +
			" OPTIONAL { " + 
				encUrl + " ma:hasSource ?source. " +
			"} " +
			" OPTIONAL { " + 
			    encUrl + " ma:description ?description ." +
			"}" +
			" OPTIONAL { " +
				encUrl + " ma:hasRelatedImage ?relImage. } " +
			" OPTIONAL { " +
				encUrl + " ma:hasRelatedLocation ?geoname. " +
			"}" + 
			"} LIMIT 30";
	}

	public String mediaItemUrlsByDate(long dateFrom, long dateTo, int limit, DateTimeFormatter... dateFormats) {
		String qPattern = load("sparql/mediaItemUrlsByDate.rq");
		String dateFilter = String.format("FILTER (%s) ",  dateFilter("?date", dateFrom, dateTo, dateFormats));
		qPattern = qPattern.replaceAll("#ReplaceDateFilter", dateFilter);
		String limitStr = String.format(" LIMIT %s", limit);
		return qPattern.replaceAll("#ReplaceLimit", limitStr);
	}

	/**
	 * Queries all mediaItemUrls without a limit <b>which can be very expensive</b> in large
	 * datasets. Use other available query alternatives to limit the result such as 
	 * {@link #mediaItemUrlsByDate(long, long, int, DateTimeFormatter...)}. 
	 * @return
	 */
	public String mediaItemUrls()  {
		return mediaItemUrls(Optional.<Integer>absent());
	}
	
	public String mediaItemUrls(Optional<Integer> optLimit) {
		String qPattern = load("sparql/mediaItemUrls.rq");
		if (optLimit.isPresent())
			return qPattern.replaceAll("#ReplaceLimit", "LIMIT " + optLimit.get());
		else return qPattern.replaceAll("#ReplaceLimit", "");
	}

	public String microPostsByKeywordFilter(List<String> allowedKeywordFilters) {
		String qPattern = load("sparql/microPostsByKeywordFilter.rq");
		String result = qPattern.replaceAll("#ReplaceByFilter", filterOneOf("?keywordFilter", asStringLits(allowedKeywordFilters)));
		return result;
	}
	
	public String mediaResourceOCRAnnotations(String mediaResUrl) {
		final String encUrl = "<" + mediaResUrl + ">";
		return "PREFIX sioc: <http://rdfs.org/sioc/ns#> " + 
			"PREFIX ma: <http://www.w3.org/ns/ma-ont#> " + 

			"SELECT ?vidTrack ?ocr { " +  
			encUrl + " ma:hasTrack ?vidTrack. " +
			" ?vidTrack a ma:VideoTrack. " +
			" ?vidTrack sioc:content ?ocr. " +
			"} LIMIT 30 ";
	}
	
	public String entityAnnotationInMediaItem(Set<String> entUrls, double confThreshold){
		if (entUrls == null || entUrls.isEmpty()) throw new IllegalArgumentException("Must pass at least one entity url");
		
		return "PREFIX xlime: <http://xlime-project.org/vocab/> " + 
				
				"SELECT distinct ?ent ?s ?c { " +
				"?s xlime:hasAnnotation ?annot. " +
				"?annot xlime:hasEntity ?ent . " + 
				" OPTIONAL { ?annot xlime:hasConfidence ?c } " +
				filterOneOfUrls("?ent", entUrls) +
				" FILTER(?c > " + confThreshold + ")" +
				"} LIMIT 30";
	}
	
	private String filterOneOfUrls(String var, Collection<String> entUrls) {
		return filterOneOf(var, bracketUrl(entUrls));
	}
	
	private String filterOneOf(String var, Collection<String> values) {
		if (values.isEmpty()) return ""; //empty filter

		String s = orEq(var, values);
		return String.format("FILTER( %s )", s);
	}

	/**
	 * Returns a String with the format
	 * <code>
	 *   (var = values(1)) || (var = values(2)) || ...
	 * </code>
	 * 
	 * If <code>values</code> is empty, an empty string is returned.
	 * If <code>values</code> only has one value, only
	 * <pre>
	 *   var '=' values(0)
	 * </pre>
	 * is returned.
	 * 
	 * @param var
	 * @param values
	 * @return
	 */
	private String orEq(String var, Collection<String> values) {
		StringBuilder sb = new StringBuilder();
		if (values.size() == 1) {
			sb.append(String.format("%s = %s", var, values.iterator().next()));
		} else for (String val: values) {
			if (sb.length() > 0) sb.append(" || ");
			sb.append(String.format("(%s = %s)", var, val));
		}
		return sb.toString();
	}

	private String bracketUrl(String url) {
		if (url.startsWith("<")) return url;
		return "<" + url + ">";
	}
	
	/**
	 * Ensure urls are bracketted
	 * @param urls
	 * @return
	 */
	private Collection<String> bracketUrl(Collection<String> urls) {
		List <String> result = new ArrayList<>();
		for (String u: urls) {
			result.add(bracketUrl(u));
		}
		return result;
	}
	
	/**
	 * Ensure string values are enclosed by quotes.
	 *  
	 * @param values
	 * @return
	 */
	private List<String> asStringLits(List<String> values) {
		List<String> result = new ArrayList<String>();
		for (String val: values) {
			result.add(asStringLit(val));
		}
		return result;
	}

	private String asStringLit(String val) {
		StringBuilder sb = new StringBuilder();
		String quote = "\"";
		if (!val.startsWith(quote)) sb.append(quote);
		sb.append(val);
		if (!val.endsWith(quote)) sb.append(quote);
		return sb.toString();
	}
	

	public String mediaItemUrisBySource(List<String> sourceUrls){
		if (sourceUrls == null || sourceUrls.isEmpty()) throw new IllegalArgumentException("Must pass at least one entity url");
		
		return "PREFIX dcterms: <http://purl.org/dc/terms/> " +
				
				"SELECT ?s { " +
				"?s dcterms:source ?source . " + 
				filterOneOfUrls("?source", sourceUrls) +
				" }";
	}

	private Object dateFilter(String var, long dateFrom, long dateTo,
			DateTimeFormatter[] dateFormats) {
		List<String> subFilters = new ArrayList<>();
		for (DateTimeFormatter df: dateFormats) {
			subFilters.add(dateFilter(var, dateFrom, dateTo, df));
		}
		if (subFilters.size() == 1) return "(" + subFilters.get(0) + ")";
		if (subFilters.size() == 0) throw new RuntimeException("dateFormat is mandatory");
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		int cnt = 0;
		for (String sf: subFilters) {
			if (cnt > 0) sb.append(" || ");
			sb.append("(").append(sf).append(")");
			cnt++;
		}
		sb.append(")");
		return sb.toString();
	}

	private String dateFilter(String var, long dateFrom, long dateTo, DateTimeFormatter formatter) {
		return String.format("%s > \"%s\"^^xsd:dateTime && %s < \"%s\"^^xsd:dateTime", 
				var, formatter.print(dateFrom), var, formatter.print(dateTo));
	}

	/**
	 * Loads a Sparql query (or query pattern) from disk or classpath.
	 * 
	 * @param path
	 * @return
	 */
	private String load(String path) {
		try {
			return loadFromFile(path);
		} catch (Exception e) {
			log.debug("Could not retrieve query(pattern) from file" + e.getLocalizedMessage() + ". Trying from classpath."); 
			try {
				return loadFromClassPath("/"+path);
			} catch (IOException e1) {
				throw new RuntimeException("Query resource not found", e1);
			}
		}
	}

	private String loadFromClassPath(String path) throws IOException {
		InputStream inputStream = getClass().getResourceAsStream(path);
		return CharStreams.toString(new InputStreamReader(
			      inputStream, Charsets.UTF_8));
	}

	private String loadFromFile(String path) throws IOException {
		File f = new File(path);
		if (!f.exists()) throw new FileNotFoundException("File does not exist " + f.getAbsolutePath());
		return Files.toString(f, Charsets.UTF_8);
	}
	
}