package eu.xlime.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Provides methods for extracting values from result-sets, objects of type <code>Map<String, Map<String, String>></code>
 * as returned by {@link SparqlClient}s.
 *  
 * @author rdenaux
 *
 */
public class ResultSetHandler {

	private String query;
	private Map<String, Map<String, String>> resultSet;

	public ResultSetHandler(String query, Map<String, Map<String, String>> resultSet) {
		super();
		this.query = query;
		this.resultSet = resultSet;
	}
	
	public Multiset<String> extractHisto(String labelVarName, String countVarName) {
		Multiset<String> result = HashMultiset.create();
		
		for (String id: resultSet.keySet()) {
				Map<String, String> tuple = resultSet.get(id);
				String label = tuple.get(labelVarName);
				long count = getLongValue(tuple, countVarName);
				result.add(label, (int)count);
		}
		
		return result;
	}
	
	public Long extractCount(String countVarName) {
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
