package eu.xlime.sparql;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class VoiDManagerImpl implements VoiDManager {

	private SparqlClient sparqler;

	public VoiDManagerImpl(SparqlClient sparqler) {
		super();
		this.sparqler = sparqler;
	}

	@Override
	public long getNumberOfTriples() {
		String query = "SELECT (COUNT(*) AS ?count) { ?s ?p ?o  }";
		return executeCountQuery(query);
	}

	@Override
	public long getNumberOfEntities() {
		String query = "SELECT (COUNT(distinct ?s) AS ?count) { ?s a []  }";
		return executeCountQuery(query);
	}

	@Override
	public long getNumberOfClasses() {
		String query = "SELECT (COUNT(distinct ?o) AS ?count) { ?s rdf:type ?o }";
		return executeCountQuery(query);
	}

	@Override
	public long getNumberOfProperties() {
		String query = "SELECT (count(distinct ?p) AS ?count) { ?s ?p ?o }";
		return executeCountQuery(query);
	}

	@Override
	public long getDistinctSubjects() {
		String query = "SELECT (COUNT(DISTINCT ?s ) AS ?count) {  ?s ?p ?o   }";
		return executeCountQuery(query);
	}

	@Override
	public long getDistinctObjects() {
		String query = "SELECT (COUNT(DISTINCT ?o ) AS ?count) {  ?s ?p ?o  filter(!isLiteral(?o)) }";
		return executeCountQuery(query);
	}

	@Override
	public Multiset<URI> getClassesByInstanceCount() {
		String query = "SELECT  ?class (COUNT(?s) AS ?count) { ?s a ?class } GROUP BY ?class ORDER BY ?count";
		return executeHistoQuery(query, "class");
	}

	@Override
	public Multiset<URI> getPropertiesByTripleCount() {
		String query = "SELECT  ?p (COUNT(?s) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count";
		return executeHistoQuery(query, "p");
	}
	
	private Multiset<URI> executeHistoQuery(String query, String varName) {
		Map<String, Map<String, String>> qResult = sparqler.executeSPARQLQuery(query);
		return mapUri(new ResultSetHandler(query, qResult).extractHisto(varName, "count"));
	}

	private Multiset<URI> mapUri(Multiset<String> inSet) {
		Multiset<URI> result = HashMultiset.create();
		for (String elt: inSet.elementSet()) {
			URI uri = java.net.URI.create(elt);
			result.add(uri, inSet.count(elt));
		}
		return result;
	}

	private long executeCountQuery(String query) {
		Map<String, Map<String, String>> resultSet = sparqler.executeSPARQLQuery(query);
		List<Long> counts = new ArrayList<Long>();
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			counts.add(getLongValue(tuple, "count"));
		}
		if (counts.isEmpty()) throw new RuntimeException("No count result for " + query + "\nresultset was: " + resultSet);
		return counts.get(0);
	}

	private Long getLongValue(Map<String, String> tuple, String varName) {
		if (!tuple.containsKey(varName)) throw new IllegalArgumentException(String.format("tuple does not contain varName %s. Tuple: %s.", varName, tuple));
		return asLong(tuple.get(varName));
	}

	private Long asLong(String sparqlStringValue) {
		return sparqlStringValueToInt(sparqlStringValue);
	}
	
	private long sparqlStringValueToInt(final String strValue) {
		String cnt = strValue.replace("^^http://www.w3.org/2001/XMLSchema#integer", "");
		cnt = cnt.replace("^^<http://www.w3.org/2001/XMLSchema#integer>", "");
		cnt = cnt.replace("\"", "");
		long count = Long.parseLong(cnt);
		return count;
	}
	
	
}
