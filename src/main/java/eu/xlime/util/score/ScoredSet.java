package eu.xlime.util.score;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableMap;


/**
 * Provides a way to keep track of a set of objects while keeping also a 
 * {@link Score} for each of the objects in the set.
 * 
 * {@link ScoredSet}s are <b>immutable</b>. By convention you should only use
 * {@link ScoredSet} to contain immutable objects. Immutability also means that 
 * the scores associated with each object cannot be changed. Typically, when 
 * <i>creating</i> a new {@link ScoredSet} you may not be sure what the final
 * score for a given object will be (multiple competing scores may have to be
 * integrated and then normalized). For this purpose, this interface defines a
 * {@link Builder} interface, which you must use to create {@link ScoredSet}s.
 *  
 * @param T some immutable object type
 * 
 * @author rdenaux
 *
 */
public interface ScoredSet<T> extends Iterable<T>,Serializable {
	
	public interface Builder<T> {
		/**
		 * Adds an object to this ScoredSet {@link Builder} with a given score.
		 * If this {@link Builder} already contains the given object, it will be
		 * kept. However, only the best score will be kept. If you add the same
		 * object twice with the same score, their justifications will be merged.
		 *   
		 * @param object
		 * @param newScore
		 */
		Builder<T> add(T object, Score newScore);
		
		/**
		 * Similar to {@link #add(Object, Score)}, however, in this case only 
		 * the new score is kept. A warning may be issued if you replace a 
		 * higher score with a lower score without taking into account the 
		 * evidence of the previous higher score that you are replacing.. 
		 * 
		 * @param object
		 * @param newScore
		 * @return
		 */
		Builder<T> replace(T object, Score newScore);
		
		/**
		 * Adds all the object-score tuples from toCopy to this builder, following 
		 * the {@link #add(Object, Score)} semantics for merging the scores for 
		 * pre-existing objects in this {@link Builder} (i.e. the best score is kept and
		 * justifications for the same score values are merged).
		 * 
		 * @param toCopy
		 * @return
		 */
		Builder<T> addAll(ScoredSet<T> toCopy);
		
		/**
		 * Adds all the object-score tuples from toCopy to this builder, following
		 * the {@link #replace(Object, Score)} semantics, i.e. old scores are
		 * discarded in favour of the new scores in toCopy (no merging of the 
		 * scores is attempted). 
		 * 
		 * @param toCopy
		 * @return
		 */
		Builder<T> replaceAll(ScoredSet<T> toCopy);
		
		/**
		 * Makes sure the score values in this builder are between 0.0 and 1.0 
		 * by dividing any values by the scoreSum. Furthermore, the sum of all
		 * the scores will be equal to 1.0. This means, in practice the
		 * individual values will be much lower than 1.0, for sets with a lot 
		 * of values.
		 * 
		 * @return
		 */
		Builder<T> normalizeBasedOnScoreSum();
		
		/**
		 * Returns the sum of all the score values in this {@link Builder}, or 
		 * 0.0 if this Builder is empty.
		 * 
		 * @return
		 */
		ScoredSet<T> build();
		
		/**
		 * Returns a representation of the currently built {@link ScoredSet} as 
		 * an {@link ImmutableMap} mapping each object to its {@link Score}
		 * @return
		 */
		ImmutableMap<T, Score> builtMap();
	}

	/**
	 * Returns the {@link Score} for the given object in this {@link ScoredSet}.
	 * If the object is not in this {@link ScoredSet}, <code>null</code> is 
	 * returned.
	 * 
	 * @param object
	 * @return
	 */
	Score getScore(T object);
	
	/**
	 * Returns a view of this {@link ScoredSet} as an unscored {@link Set}.
	 * 
	 * @return
	 */
	Set<T> unscored();
	
	/**
	 * Returns a view of this {@link ScoredSet} as an unscored {@link List}, 
	 * that is ordered from highest score to lowest score.
	 * 
	 * @return
	 */
	List<T> asList();
	
	/**
	 * Returns the number of objects contained in this {@link ScoredSet}.
	 * @return
	 */
	int size();
	
	/**
	 * Returns whether this {@link ScoredSet} is empty (i.e. whether it's {@link #size()} equals <code>0</code>
	 * @return
	 */
	boolean isEmpty();
	
}
