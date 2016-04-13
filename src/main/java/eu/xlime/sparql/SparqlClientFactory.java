package eu.xlime.sparql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xlime.Config;

/**
 * Provides {@link SparqlClient} instances.
 * 
 * @author RDENAUX
 *
 */
public class SparqlClientFactory {

	private static final Logger log = LoggerFactory.getLogger(SparqlClientFactory.class);
	
	private static SparqlClient xlimeSparqlClient;
	
	private static SparqlClient dbpediaSparqlClient;
	
	public SparqlClient getXliMeSparqlClient() {
		if (xlimeSparqlClient != null) return xlimeSparqlClient;
		Config cfg = new Config();
		xlimeSparqlClient =
				createEndpointClient(cfg.get(Config.Opt.SparqlEndpoint),
						new UserPassword(cfg.get(Config.Opt.SparqlUname), cfg.get(Config.Opt.SparqlPassw)), 
						cfg.getDouble(Config.Opt.SparqlRate));
		return xlimeSparqlClient;
	}
	
	public SparqlClient getDBpediaSparqlClient() {
		if (dbpediaSparqlClient != null) return dbpediaSparqlClient;
		Config cfg = new Config();
		dbpediaSparqlClient =
				createEndpointClient(
						cfg.get(Config.Opt.DBpediaSparqlEndpoint),
						cfg.getDouble(Config.Opt.DBpediaSparqlRate));
		return dbpediaSparqlClient;
	}
	

	/**
	 * Creates an SparqlClient to access a remote sparql endpoint. The
	 * returned {@link SparqlClient} will use the standard http interfaces
	 * defined as part of the SPARQL W3C recommendation to interact with the
	 * endpoint.
	 *
	 * @param endpointUrl a {@link java.lang.String} object.
	 * @param maxQueriesPerSecond the maximum number of queries per second that
	 *  the returned {@link SparqlClient} can make to the endpoint. This
	 *  means that calls to {@link SparqlClient}s methods may take a while
	 *  to complete, depending on how much you want to throttle your calls to
	 *  the endpoint. Typically, for public endpoints you don't want to make
	 *  more than a few calls per second to avoid overloading the endpoint (
	 *  though you also need to into account how many sparql queries you will
	 *  make overall). For your own endpoint, you can be more aggressive, since
	 *  you can monitor and tweak your own endpoints, so you can go as high as
	 *  you want (e.g. 10000 queries per second?)
	 * @return a {@link SparqlClient} object.
	 */
	SparqlClient createEndpointClient(String endpointUrl, double maxQueriesPerSecond) {
		return new OpenRDFSparqlClientImpl(endpointUrl, maxQueriesPerSecond);
	}
	
	SparqlClient createEndpointClient(String endpointUrl, UserPassword userPassword, double maxQueriesPerSecond) {
		return new OpenRDFSparqlClientImpl(endpointUrl, userPassword, maxQueriesPerSecond);		
	}

	/**
	 * Encodes authentication information to connect to a protected sparql enpoint
	 * 
	 * @author RDENAUX
	 *
	 */
	public static class UserPassword {
		final String username;
		final String password;
		public UserPassword(String username, String password) {
			super();
			this.username = username;
			this.password = password;
		}
	}
	
	
}
