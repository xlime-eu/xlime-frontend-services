package eu.xlime.util.score;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Special type of a {@link ScoredSet} where the scored objects are all 
 * <b>directed binary relations</b> from a source type to a destination type.
 * 
 * This interface provides generic methods for manipulating the {@link ScoredSet}s 
 * for example by giving access to only the set of source objects. 
 * 
 * @param R the type used to represent the binary relation contained by this 
 * 	{@link ScoredRelSet}. As any {@link ScoredSet}, this type needs to be immutable.
 * 	Furthermore, to enable the implementation of the interface, this type must 
 * 	implement the {@link Tuple2} interface.  
 * @param F the type of the source objects in the relations contained by this 
 * 	{@link ScoredRelSet}.
 * @param T the type of the destination objects in the relations contained by this
 * 	{@link ScoredRelSet}.
 * 
 * @author rdenaux
 *
 */
public interface ScoredRelSet<R extends Tuple2<F,T>, F, T> extends ScoredSet<R> {

	interface Builder<R extends Tuple2<F,T>, F, T> extends ScoredSet.Builder<R> {

		/**
		 * Adds a new relation to this {@link Builder}, by specifying the relation
		 * as a source and a destination. The builder is responsible for creating
		 * the correct R object from the source and destination objects.
		 *  
		 * Furthermore, the semantics of this add method are the same as those of
		 * the {@link #add(Object, Score)} method.
		 * 
		 * @param source
		 * @param destination
		 * @param newScore
		 * @return
		 */
		Builder<R, F, T> add(F source, T destination, Score newScore);

		/**
		 * Overrides the {@link ScoredSet.Builder#add(Object, Score)} definition 
		 * in order to return the correct ScoredRelSet {@link Builder}.
		 * 
		 * @param object
		 * @param newScore
		 * @return
		 */
		Builder<R, F, T> add(R object, Score newScore);

		/**
		 * Overrides the {@link ScoredSet.Builder#addAll(ScoredSet)} definition 
		 * in order to return the correct ScoredRelSet {@link Builder}.
		 * 
		 * @param toCopy
		 * @return
		 */
		Builder<R, F, T> addAll(ScoredSet<R> toCopy);

		/**
		 * Provides an alternative way to add many relations by adding all the
		 * relations as given by a toCopy {@link Map} between the sources and
		 * the destinations of the relations. Each relation is added according 
		 * to the {@link #add(Tuple2, Score)} semantics.
		 * 
		 * @param toCopy
		 * @return
		 */
		Builder<R, F, T> addAll(Map<F, ScoredSet<T>> toCopy);

		/**
		 * Convenience method for adding various relations to this {@link Builder}
		 * by specifying several relations between a single source and a number 
		 * of scored destinations.
		 * 
		 * @param source
		 * @param destinations
		 * @return
		 */
		<S extends T> Builder<R, F, T> addAll(F source, ScoredSet<S> destinations);
		
		/**
		 * Overrides the {@link ScoredSet.Builder#normalizeBasedOnScoreSum()} definition 
		 * in order to return the correct ScoredRelSet {@link Builder}.
		 */
		Builder<R, F, T> normalizeBasedOnScoreSum();
		
		/**
		 * Overrides the {@link ScoredSet.Builder#build()} definition 
		 * in order to return the correct {@link ScoredRelSet}.
		 */
		ScoredRelSet<R, F, T> build();
	}
	
	/**
	 * Returns a summary giving a short overview of the relations contained 
	 * by this {@link ScoredRelSet}.
	 * 
	 * For example: "x relations between y sources and z destinations."
	 * 
	 * @return
	 */
	String getSummary();
	
	/**
	 * Returns the set of all destination objects contained in this 
	 * {@link ScoredRelSet}.
	 * 
	 * @return
	 */
	Set<T> getToSet();
	
	/**
	 * Returns the set of all the source objects contained in this 
	 * {@link ScoredRelSet}.
	 * 
	 * @return
	 */
	Set<F> getFromSet();

	/**
	 * Flatten this {@link ScoredRelSet} into a 
	 * {@link ScoredSet} containing the {@link #getFromSet()} elements from the
	 * relations in this {@link ScoredSet}. The scores of the resulting set are 
	 * calculated in a way that: source elements with lots of (highly scored) 
	 * relations will have a higher score in the resulting {@link ScoredSet} 
	 * and from elements with few (and lowly scored) relations will have a low 
	 * score in the result.   
	 *  
	 * @return
	 */
	ScoredSet<F> flattenToFromSet();
	
	/**
	 * Returns the maximum number of relations from a single source in this 
	 * {@link ScoredRelSet} to destination objects.
	 *  
	 * @return
	 */
	int maxNumberOfRelsFromASource();
	
	/**
	 * Returns a {@link ScoredSet} containing all the destinations that are 
	 * related to the <code>source</code> object. The scores for each destination
	 * object are the same scores as the scores for the relation between 
	 * <code>source</code> and that destination. 
	 *  
	 * @param source
	 * @return
	 */
	ScoredSet<T> children(F source);
	
	/**
	 * Returns a {@link ScoredSet} containing all the sources that are
	 * related to the <code>destination</code> object. The scores for each 
	 * destination object are the same scores as the scores for the relation 
	 * between <code>destination</code> and that source.
	 * 
	 * @param destination
	 * @return
	 */
	ScoredSet<F> parents(T destination);
	
	/**
	 * Returns a view of this {@link ScoredRelSet} as a {@link Map} linking 
	 * the sources to the destinations in this relation set.
	 * 
	 * @return
	 */
	Map<F, List<T>> asMap();
}
