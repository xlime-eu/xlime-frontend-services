package eu.xlime.util.score;

/**
 * Wrapper for adding a {@link Double}-valued score (and an optional 
 * justification for that score) to an object of type T. 
 * 
 * Typically the object represents some statement (e.g. that a given token 
 * represents a given entity, or that two entities are related via a specific 
 * relation), while the score gives a confidence value about the veracity of the
 * statement. Finally, the optional justification contains additional background
 * information that justifies the value of the score for the object.  
 * 
 * @author rdenaux
 *
 * @param <T>
 */
public interface Scored<T> extends Score {

	/**
	 * Returns the object that is scored by this.
	 * 
	 * @return
	 */
	T getObject();
	
}
