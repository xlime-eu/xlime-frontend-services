package eu.xlime.datasum;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multiset;

import eu.xlime.datasum.bean.HistogramItem;
import eu.xlime.sparql.ResultSetHandler;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.sparql.VoiDManager;
import eu.xlime.sparql.VoiDManagerImpl;

/**
 * Class to get access to the Sparql endpoint and retrieve the dataset summary information
 * 
 * @author Nuria Garcia
 * {@link}: ngarcia@expertsystem.com
 *
 */

public class SparqlDatasetSummaryDaoImpl {

	private static SparqlClient ontoMan;
	private static VoiDManager ontoManager;

	public SparqlDatasetSummaryDaoImpl(){
		ontoManager = (VoiDManager) getOntologyAccess();
	}	

	private VoiDManager getOntologyAccess(){
		if (ontoManager != null) return ontoManager;
		ontoMan = new SparqlClientFactory().getXliMeSparqlClient();
		VoiDManager voidMan = new VoiDManagerImpl(ontoMan);
		return (VoiDManager) voidMan;
	}

	public Long getNumTriples(){
		return ontoManager.getNumberOfTriples();
	}

	public Long getNumEntities(){ //Time out
		return ontoManager.getNumberOfEntities();			
	}

	public Long getNumSubjects(){ //Time out
		return ontoManager.getDistinctSubjects();
	}

	public Long getNumPredicates(){ //Time out
		return ontoManager.getNumberOfProperties();
	}

	public Long getNumObjects(){ //Time out
		return ontoManager.getDistinctObjects();
	}
	
	public Long getNumActivities(){
		String query = "PREFIX prov: <http://www.w3.org/ns/prov#> "
				+ "SELECT count(*) AS ?count WHERE {"
				+ " ?s a prov:Activity .}";
		Map<String, Map<String, String>> result = ontoMan.executeSPARQLQuery(query);
		return new ResultSetHandler(query, result).extractCount("count");
	}

	public Long getNumMicroposts(){
		String query = "PREFIX sioc: <http://rdfs.org/sioc/ns#> "
				+ "SELECT count(*) AS ?count WHERE {"
				+ " ?s a sioc:MicroPost .}";
		Map<String, Map<String, String>> result = ontoMan.executeSPARQLQuery(query);
		return new ResultSetHandler(query, result).extractCount("count");
	}
	
	public Long getNumMicropostsbyFilter(String filter){
		String query = "PREFIX sioc: <http://rdfs.org/sioc/ns#> "
				+ "PREFIX xlime: <http://xlime-project.org/vocab/> "
				+ "SELECT count(distinct ?s) AS ?count WHERE { " 
				+ "?s a sioc:MicroPost. " 
				+ "?s xlime:keywordFilterName ?filter. ";
		
		String[] params = filter.split(",");
		String filter_query = "";
		for (String p : params) {
			filter_query += "?filter = \"" + p + "\" || ";
		}
		filter_query = filter_query.substring(0, filter_query.length()-4);
		query += "FILTER (" + filter_query + ")}";
		Map<String, Map<String, String>> result = ontoMan.executeSPARQLQuery(query);
		return new ResultSetHandler(query, result).extractCount("count");
	}

	public Long getNumNewsarticles(){
		String query = "PREFIX kdo: <http://kdo.render-project.eu/kdo#> "
				+ "SELECT count(*) AS ?count WHERE { "
				+ "?s a kdo:NewsArticle .}";
		Map<String, Map<String, String>> result = ontoMan.executeSPARQLQuery(query);
		return new ResultSetHandler(query, result).extractCount("count");
	}

	public Long getNumMediaresources(){
		String query = "PREFIX ma: <http://www.w3.org/ns/ma-ont#> "
				+ "SELECT count(*) AS ?count WHERE { "
				+ "?s a ma:MediaResource .}";
		Map<String, Map<String, String>> result = ontoMan.executeSPARQLQuery(query);
		return new ResultSetHandler(query, result).extractCount("count");
	}

	public List<HistogramItem> getInstancesPerClass(){
		List<HistogramItem> items = new ArrayList<HistogramItem>();
		Multiset<URI> instances = ontoManager.getClassesByInstanceCount();	
		for (URI uri : instances.elementSet()) {
			HistogramItem item = new HistogramItem();
			item.setItem(uri.toString());
			item.setCount((long)instances.count(uri));
			items.add(item);
		}			
		return items; 
	}

	public List<HistogramItem> getTriplesPerProperty(){ //Time out
		List<HistogramItem> items = new ArrayList<HistogramItem>();
		Multiset<URI> instances = ontoManager.getPropertiesByTripleCount();
		for (URI uri : instances.elementSet()) {
			HistogramItem item = new HistogramItem();
			item.setItem(uri.toString());
			item.setCount((long)instances.count(uri));
			items.add(item);
		}			
		return items;
	}
}
