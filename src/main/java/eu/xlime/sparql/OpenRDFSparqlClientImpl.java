package eu.xlime.sparql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.httpclient.HttpClient;
import org.openrdf.http.client.HTTPClient;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.RateLimiter;

import eu.xlime.sparql.SparqlClientFactory.UserPassword;

public class OpenRDFSparqlClientImpl implements SparqlClient {

	private final Repository repository;

	private final OpenRDFConnection con;
    
	private final RateLimiter sparqlLimiter;

    private static Logger logger = LoggerFactory.getLogger(OpenRDFSparqlClientImpl.class);
	
	/**
	 * <p>Creates a {@link OpenRDFSparqlClientImpl} .</p>
	 *
	 * @param endpointUrl a {@link java.lang.String} object.
	 * @param maxQueriesPerSecond a double.
	 */
	public OpenRDFSparqlClientImpl(String endpointUrl, double maxQueriesPerSecond) {
		this(endpointUrl, null, maxQueriesPerSecond);
	}
    	
	public OpenRDFSparqlClientImpl(String endpointUrl, UserPassword userPassword, double maxQueriesPerSecond) {
		this(newRepo(endpointUrl, Optional.fromNullable(userPassword)), maxQueriesPerSecond);
	}
	
	private static Repository newRepo(String endpointUrl, Optional<UserPassword> optUP) {
		logger.debug("Accessing remote sparql endpoint '" + endpointUrl + "' ");
		// this used to be an HTTPRepository, but that assumes that doesn't work with authentication as it 
		// assumes that the server is a Sesame server.
		MySparqlRepo result = new MySparqlRepo(endpointUrl);
		if (optUP.isPresent()) {
			UserPassword up = optUP.get();
			result.setUsernameAndPassword(up.username, up.password);
			result.setPreemptiveAuth(false);
		}
		return result;
	}
	
	/**
	 * Provide access to underlying HttpClient
	 * 
	 * @author RDENAUX
	 *
	 */
	private static class MyHttpClient extends HTTPClient {

		public MyHttpClient() {
			super();
		}

		public HttpClient myGetHttpClient() {
			return getHttpClient();
		}
	}

	/**
	 * Fixes authentication issue with default {@link SPARQLRepository}.
	 * 
	 * @author RDENAUX
	 *
	 */
	private static class MySparqlRepo extends SPARQLRepository {
		
		public MySparqlRepo(String queryEndpointUrl, String updateEndpointUrl) {
			super(queryEndpointUrl, updateEndpointUrl);
		}

		public MySparqlRepo(String endpointUrl) {
			super(endpointUrl);
		}

		public void setPreemptiveAuth(boolean value) {
			((MyHttpClient)getHTTPClient()).myGetHttpClient().getParams().setAuthenticationPreemptive(value);
		}
		
		public HTTPClient myGetHttpClient() {
			return getHTTPClient();
		}

		@Override
		protected HTTPClient createHTTPClient() {
			return new MyHttpClient();
		}
		
		
	}
	
    /**
     * <p>Constructor for SesameDAO.</p>
     *
     * @param repo a {@link org.openrdf.repository.Repository} object.
     * @param maxQueriesPerSecond a double.
     */
    public OpenRDFSparqlClientImpl(Repository repo, double maxQueriesPerSecond){
    	sparqlLimiter = RateLimiter.create(maxQueriesPerSecond);
		this.repository = repo;
	    this.con = new OpenRDFConnection(repository);
	    //try to execute a basic query in order to make sure SesameDAO is functional
	    execQueryAndReturnColumn( 
					"SELECT ?s WHERE {?s a ?o} LIMIT 1", 
					"s");
    }
	
    
    public <T> T executeSPARQLQuery(String query, Class<T> desiredResultType) {
    	return resultConverter(doExecuteQuery(query), desiredResultType);
    }

    @SuppressWarnings("unchecked")
	private <T> T resultConverter(TupleQueryResult tqr, Class<T> desiredResultType) {
    	if (TupleQueryResult.class.equals(desiredResultType)) return (T)tqr;
    	else if (Map.class.equals(desiredResultType)) return (T)asMap(tqr);
    	else throw new RuntimeException("Unsupported result type " + desiredResultType);
    }
    

	private TupleQueryResult doExecuteQuery(String query) {
		try {
			return doExecuteQuery(query, -1);
		} catch (TimeoutException e) {
			throw new RuntimeException(e); //this should never happen as no timeout is passed
		}
	}
    
	private TupleQueryResult doExecuteQuery(String query, long timeout) throws TimeoutException {
		sparqlLimiter.acquire();
		if (timeout > 0) {
			return con.evaluate(query, timeout);
		} else return con.evaluate(query);
	}
    
	@Override
	public Map<String, Map<String, String>> executeSPARQLQuery(String query) {
		try {
			return executeSPARQLQuery(query, -1);
		} catch (TimeoutException e) {
			throw new RuntimeException(e); //this should never happen as no timeout is passed
		}
	}

	@Override
	public Map<String, Map<String, String>> executeSPARQLQuery(String query,
			long timeout) throws TimeoutException {
		Map<String, Map<String,String>> results = new HashMap<String, Map<String,String>>();

		TupleQueryResult tqr = doExecuteQuery(query, timeout);
		results = asMap(tqr);
		
		return results; 
	}

	@Override
	public Map<String, Map<String, String>> executeSPARQLOrEmpty(String query,
			long timeout) {
		try {
			return executeSPARQLQuery(query, timeout);
		} catch (TimeoutException e) {
			logger.debug("Query timed out, returning empty resultset.", e);
			return ImmutableMap.of();
		}
	}

	private Map<String, Map<String, String>> asMap(TupleQueryResult tqr) {
		Map<String, Map<String,String>> results = new HashMap<String, Map<String,String>>();
		int counter = 0;
		try {
			List<String> vars = tqr.getBindingNames();
		
			while(tqr.hasNext()) {
				BindingSet bs = tqr.next();
				counter++;
				Map<String, String> tuple = new HashMap<String, String>();

				for (String var : vars) {
					// System.out.print(soln.get(vars.get(k))+"\t");
					Value val = bs.getValue(var);
					if (val != null)
						tuple.put(var, val.stringValue());
				}
				results.put(Integer.toString(counter), tuple);
				// System.out.println();
			}
		} catch (QueryEvaluationException e) {
			logger.error("Error converting query result to Map", e);
		}
		return results;
	}
		
	private List<String> execQueryAndReturnColumn(
			String query, String column) {
		sparqlLimiter.acquire();
		return con.getColumn(query, column);
	}
	

}
