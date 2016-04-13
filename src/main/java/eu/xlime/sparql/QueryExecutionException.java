package eu.xlime.sparql;

public class QueryExecutionException extends RuntimeException {

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
