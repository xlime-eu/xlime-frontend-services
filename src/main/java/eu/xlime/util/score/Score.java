package eu.xlime.util.score;

import com.google.common.base.Optional;

/**
 * Represents an (optionally justified) numeric score for some external object 
 * or predicate.
 * 
 * Typically, this interface is used internally while the Scored
 *   
 * @author rdenaux
 *
 */
public interface Score {
	
	/**
	 * Returns the score value. For a normalized score, this will be a value 
	 * between 0.0 and 1.0. But note that score of an object may be dependent on
	 * the context (especially during computation, scores may be out of the 0.0 
	 * to 1.0 range before normalization is applied). 
	 * @return
	 */
	Double getValue();
	
	/**
	 * A non-null, {@link Optional} justification for the score if available. 
	 */
	Optional<String> getJustification();
}
