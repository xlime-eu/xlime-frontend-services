package eu.xlime.sparql;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.format.DateTimeFormatter;

public class SparqlQueryFactory {

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
			  " { " + encUrl + " owl:sameAs ?sameAs. } " +
			  " UNION { ?sameAs owl:sameAs " + encUrl + " } " + 
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
	
	
	public String microPostDetails(String url) {
		//TODO: I think VICO is now uploading more info about creator... test
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
		return "PREFIX xlime: <http://xlime-project.org/vocab/> " + 
				"PREFIX dcterms: <http://purl.org/dc/terms/> " +
				"PREFIX sioc: <http://rdfs.org/sioc/ns#> " + 
				"PREFIX kdo: <http://kdo.render-project.eu/kdo#> " +
				"PREFIX ma: <http://www.w3.org/ns/ma-ont#> " + 

				"SELECT distinct ?s ?date ?type { " + 
				"?s a ?type. " + 
				" { " + 
				"  ?s a kdo:NewsArticle." +
				"  ?s dcterms:created ?date." +
				" } UNION { " +
				"  ?s a ma:MediaResource. " +
				"  ?s ma:date ?date. " +  
				" } UNION { " +
				"  ?s a sioc:MicroPost. " + 
				
				"  ?s dcterms:created ?date. " +
				" } " +
				String.format(" FILTER (%s) ", dateFilter("?date", dateFrom, dateTo, dateFormats)) +
				String.format("} LIMIT %s", limit);
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
}
