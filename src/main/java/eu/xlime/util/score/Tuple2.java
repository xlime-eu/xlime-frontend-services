package eu.xlime.util.score;

import java.io.Serializable;

/**
 * 
 * @author rdenaux
 *
 * @param <F>
 * @param <T>
 */
public interface Tuple2<F, T>  {

	/**
	 * Provides metadata information about a {@link Tuple2} implementation of 
	 * type R, relating objects of type F to objects of type T.
	 * 
	 * @author rdenaux
	 *
	 * @param <R>
	 * @param <F>
	 * @param <T>
	 */
	interface Tuple2Metadata<R extends Tuple2<F, T>, F, T> extends Serializable {
		/**
		 * Returns a name for the relationship described by type <code>R</code>
		 * @return
		 */
		String getRelationName();
		
		/**
		 * Returns a name for the set of source (i.e. from) objects of type 
		 * <code>F</code>.
		 * 
		 * @return
		 */
		String sourcesName();
		
		/**
		 * Returns a name for the set of destination (i.e. to) objects of type
		 * <code>T</code>
		 * @return
		 */
		String destinationsName();
	}
	
	/**
	 * Creates instances of {@link Tuple2} implementations of type R.
	 * 
	 * @author rdenaux
	 *
	 * @param <R>
	 * @param <F>
	 * @param <T>
	 */
	interface Builder<R extends Tuple2<F, T>, F, T> {
		/**
		 * Creates a {@link Tuple2} implementation of type R mapping a 
		 * <code>fromObject</code> to a <code>toObject</code>.
		 * 
		 * @param fromObject a non-null object
		 * @param toObject a non-null object
		 * @return a new {@link Tuple2} object of type R relating the 
		 * <code>fromObject</code> to the <code>toObject</code>.
		 */
		R buildTuple2(F fromObject, T toObject);
		
		/**
		 * Returns the metadata for the {@link Tuple2} implementation created 
		 * by this {@link Builder}.
		 * @return
		 */
		Tuple2Metadata<R, F, T> relationMetadata();
	}
	
	/**
	 * Returns the source object of this {@link Tuple2}.
	 * 
	 * @return
	 */
	F getFrom();
	
	/**
	 * Returns the destination object of this {@link Tuple2}.
	 * @return
	 */
	T getTo();
	
}
