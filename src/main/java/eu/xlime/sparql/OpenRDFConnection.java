package eu.xlime.sparql;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Manages and provides a wrapper for {@link RepositoryConnection}.
 * 
 * @author RDENAUX
 *
 */
public class OpenRDFConnection {

    private Repository repository;
    private RepositoryConnection connection = null;

	public OpenRDFConnection(Repository rep){
        this.repository = rep;
		try {
		    repository.initialize();
			connection = repository.getConnection();	// starts connection pooling
		} catch (RepositoryException e) {
			throw new IllegalArgumentException("Failed to connect to repository",e);
		}
	}
    

	public TupleQueryResult evaluate(String sparqlQuery) {
		TupleQueryResult result = null;
		try{
			result = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate();
		} catch (RepositoryException e) {
			throw new QueryExecutionException("Failed to evaluate SPARQL query:\n"+sparqlQuery,e);
		} catch (MalformedQueryException e) {
			throw new QueryExecutionException("Failed to evaluate SPARQL query:\n"+sparqlQuery,e);
		} catch (QueryEvaluationException e) {
			throw new QueryExecutionException("Failed to evaluate SPARQL query:\n"+sparqlQuery,e);
		}
		return result;
	}
	
	public List<String> getColumn(String sparqlQuery, String variableName) {
      	ArrayList<String> column = new ArrayList<String>();
		try {
			TupleQueryResult result = evaluate(sparqlQuery);
			while (result.hasNext()){
				String value = result.next().getValue(variableName).toString();
				column.add(value);
			}
		} catch (QueryEvaluationException e) {
			throw new QueryExecutionException("Failed to evaluate SPARQL query:\n"+sparqlQuery,e);
		}
		return column;
    }
	
}
