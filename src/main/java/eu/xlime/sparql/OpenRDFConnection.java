package eu.xlime.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.DirectoryWalker.CancelException;
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
    private static final ExecutorService exec = Executors.newFixedThreadPool(4);

    
	public OpenRDFConnection(Repository rep){
        this.repository = rep;
		try {
		    repository.initialize();
			connection = repository.getConnection();	// starts connection pooling
		} catch (RepositoryException e) {
			throw new IllegalArgumentException("Failed to connect to repository",e);
		}
	}
    
	public TupleQueryResult evaluate(String sparqlQuery, long timeOut) throws TimeoutException {
		Future<TupleQueryResult> result = exec.submit(callableEvaluate(sparqlQuery));
		try {
			return result.get(timeOut, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		} 
	}
	
	private Callable<TupleQueryResult> callableEvaluate(final String sparqlQuery) {
		return new Callable<TupleQueryResult>() {
			@Override
			public TupleQueryResult call() throws Exception {
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
		};
	}
	
	public TupleQueryResult evaluate(String sparqlQuery) {
		try {
			return callableEvaluate(sparqlQuery).call();
		} catch (Exception e) {
			if (e instanceof QueryExecutionException) throw (QueryExecutionException)e;
			else throw new QueryExecutionException(e);
		}
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
