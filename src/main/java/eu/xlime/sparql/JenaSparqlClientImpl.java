package eu.xlime.sparql;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class JenaSparqlClientImpl implements SparqlClient {

	private static final Logger log = LoggerFactory.getLogger(JenaSparqlClientImpl.class);
	
	private final Dataset dataset;
	
	public JenaSparqlClientImpl(Dataset dataset) {
		super();
		this.dataset = dataset;
	}

	@Override
	public Map<String, Map<String, String>> executeSPARQLQuery(String query) {
		return resultConverter(doExecuteQuery(query), Map.class);
	}

	@Override
	public Map<String, Map<String, String>> executeSPARQLQuery(String query,
			long timeout) throws TimeoutException {
		return resultConverter(doExecuteQuery(query), Map.class); //todo: use timeout
	}

	@Override
	public Map<String, Map<String, String>> executeSPARQLOrEmpty(String query,
			long timeout) {
		return resultConverter(doExecuteQuery(query), Map.class); //todo: use timeout
	}

	@Override
	public <T> T executeSPARQLQuery(String query, Class<T> desiredResultType) {
		return resultConverter(doExecuteQuery(query), desiredResultType);
	}

	private ResultSet doExecuteQuery(String query) {
		try {
			Query q = QueryFactory.create(query);
			QueryExecution qexec = QueryExecutionFactory.create(q, dataset);
			return qexec.execSelect();
		} catch (Throwable e) {
			log.error("Error executing query " + query, e);
			throw new RuntimeException("Error executing query " + query, e);
		}
	
	}
    
    @SuppressWarnings("unchecked")
	private <T> T resultConverter(ResultSet rs, Class<T> desiredResultType) {
    	if (ResultSet.class.equals(desiredResultType)) return (T)rs;
    	else if (Map.class.equals(desiredResultType)) return (T)asMap(rs);
    	else throw new RuntimeException("Unsupported result type " + desiredResultType);
    }
    
	private Map<String, Map<String, String>> asMap(ResultSet rs) {
		Map<String, Map<String,String>> results = new HashMap<String, Map<String,String>>();
		int counter = 0;
		for (; rs.hasNext(); ) {
			QuerySolution soln = rs.nextSolution();
			results.put(Integer.toString(counter++), asTuple(soln));
		}
		return results;
	}

	private Map<String, String> asTuple(QuerySolution soln) {
		Map<String, String> tuple = new HashMap<String, String>();
		for (String var: ImmutableList.copyOf(soln.varNames())) {
			RDFNode node = soln.get(var);
			if (node != null)
				tuple.put(var, stringValue(node));
		}
		return tuple;
	}

	private String stringValue(RDFNode node) {
		if (node.isLiteral()) return node.asLiteral().getString();
		return node.toString();
	}
    
	
}
