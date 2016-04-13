package eu.xlime.sparql;

import java.util.Map;

public interface SparqlClient {

	/**
	 * Executes the input sparql <code>query</code> and returns the resultset as 
	 * a {@link Map} of results. The keys are simple result identifiers and the 
	 * values (or results) are themselves {@link Map}s from variable names of 
	 * the sparql query to their {@link String} values.
	 * 
	 * <b>Note that the resultset looses the type of literals as well as the 
	 * language tag of strings literals.</b> If you require that information you 
	 * need to use a particular instance of {@link SparqlClient} that provides such 
	 * information.  
	 *
	 * @param query a valid SPARQL query
	 * @return a {@link java.util.Map} object.
	 */
	Map<String, Map<String, String>> executeSPARQLQuery(String query);
	
	/**
	 * Executes the input sparql query and attempts to return the resultset
	 * as a requested <code>desiredResultType</code>
	 *  
	 * @param query a valid SPARQL query
	 * @param desiredResultType indicates how the resultset should be encoded. 
	 *  For example TupleQueryResult if you tend to use OpenRDF/Sesame or 
	 *  ResultSet if you tend to use jena. 
	 */
	<T> T executeSPARQLQuery(String query, Class<T> desiredResultType);

}
