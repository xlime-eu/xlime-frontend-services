package eu.xlime.sparql;

/**
 * {@link RuntimeException} thrown when some error occurred when executing a Sparql query.
 *  
 * @author RDENAUX
 *
 */
public class QueryExecutionException extends RuntimeException {

	private static final long serialVersionUID = 8325161603987788897L;

	public QueryExecutionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public QueryExecutionException(String arg0) {
		super(arg0);
	}

	public QueryExecutionException(Throwable arg0) {
		super(arg0);
	}

	
}
