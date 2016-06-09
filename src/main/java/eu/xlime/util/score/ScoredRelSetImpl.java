package eu.xlime.util.score;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;

import eu.xlime.util.score.ScoredSetImpl.ScoredSetImplBuilder;
import eu.xlime.util.score.Tuple2.Tuple2Metadata;

/**
 * Default implementation of a {@link ScoredRelSet}.
 * 
 * It uses a delegate {@link ScoredSet} to store the set of scored relations and
 * implements the remaining methods in the {@link ScoredRelSet} interface. 
 * 
 * @author rdenaux
 *
 * @param <R> The (binary) relation type (essentially a Tuple2 from F to T
 * @param <F> The source type
 * @param <T> The destination type
 */
public class ScoredRelSetImpl<R extends Tuple2<F, T>, F, T> implements ScoredRelSet<R, F, T>, Serializable {

	private static final long serialVersionUID = 3262431903548544057L;

	private final static Logger log = LoggerFactory.getLogger(ScoredRelSet.class);
	
	private final static ScoreFactory scoreFactory = ScoreFactory.instance;
	
	private final ScoredSet<R> relSet;

	/**
	 * Index to find subsets of {@link #relSet} based on their source element.
	 * This is a lazy field which is generated as necessary in order to save 
	 * memory. 
	 */
	private ImmutableSetMultimap<F, R> relSetFromIndex;
	
	/**
	 * The metadata about the specific relation type contained by this 
	 * {@link ScoredRelSet}.
	 */
	private final Tuple2Metadata<R, F, T> relMeta;
	
	protected ScoredRelSetImpl(ScoredSet<R> delegate, Tuple2Metadata<R, F, T> aRelationMetadata) {
		if (aRelationMetadata == null) throw new NullPointerException("relation metadata");
		
		relSet = delegate;
		relMeta = aRelationMetadata;
	}
	
	public static class ScoredRelSetImplBuilder<R extends Tuple2<F, T>, F, T> 
		extends ScoredSetImplBuilder<R> implements ScoredRelSet.Builder<R, F, T> {

		private final Tuple2.Builder<R, F, T> tupleBuilder;
		
		protected ScoredRelSetImplBuilder(Tuple2.Builder<R, F, T> aTupleBuilder) {
			if (aTupleBuilder == null) throw new NullPointerException("tupleBuilder");
			tupleBuilder = aTupleBuilder;
		}
		
		@Override
		public ScoredRelSet.Builder<R, F, T> add(F source,
				T destination, Score newScore) {
			return add(tupleBuilder.buildTuple2(source, destination), newScore);
		}

		@Override
		public ScoredRelSet.Builder<R, F, T> add(R object,
				Score newScore) {
			super.add(object, newScore);
			return this;
		}

		@Override
		public ScoredRelSet.Builder<R, F, T> addAll(
				ScoredSet<R> toCopy) {
			super.addAll(toCopy);
			return this;
		}

		@Override
		public ScoredRelSet.Builder<R, F, T> addAll(Map<F, ScoredSet<T>> toCopy) {
			for (F source: toCopy.keySet()) {
				addAll(source, toCopy.get(source));
			}
			return this;
		}

		@Override
		public <S extends T> Builder<R, F, T> addAll(F source, ScoredSet<S> destinations) {
			for (S destination: destinations) {
				add(source, destination, 
						destinations.getScore(destination));
			}
			return this;
		}

		@Override
		public ScoredRelSet.Builder<R, F, T> normalizeBasedOnScoreSum() {
			super.normalizeBasedOnScoreSum();
			return this;
		}

		@Override
		public ScoredRelSet<R, F, T> build() {
			return new ScoredRelSetImpl(buildScoredSet(), getRelationMetadata());
		}

		protected final Tuple2Metadata<R, F, T> getRelationMetadata() {
			return tupleBuilder.relationMetadata();
		}
		
	}
	
	/**
	 * Returns the underlying ScoredSet of relation instances.
	 *  
	 * @return
	 */
	public final ScoredSet<R> getRelSet() {
		return relSet;
	}

	/**
	 * Returns the relation metadata for this {@link ScoredRelSetImpl}.
	 * 
	 * @return
	 */
	public final Tuple2Metadata<R, F, T> getRelMeta() {
		return relMeta;
	}

	/**
	 * Prints a summary of this {@link ScoredRelSet}. E.g. 
	 * <pre>
	 * 	5 TermsToNEs between 2 Term instances and 3 GenericElement instances.
	 * </pre>
	 */
	public String getSummary() {
		return String.format("%s %s between %s %s and %s %s.", 
				relSet.size(), relMeta.getRelationName(), 
				getFromSet().size(), relMeta.sourcesName(), 
				getToSet().size(), relMeta.destinationsName());
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScoredRelSet [");
		if (relSet != null)
			builder.append("relSet=").append(relSet).append(", ");
		if (relMeta != null)
			builder.append("relMeta=").append(relMeta);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Attempts to flatten this {@link ScoredSet} of {@link Tuple2}s into a 
	 * {@link ScoredSet} containing only the {@link #getFromSet()} elements in the
	 * {@link Tuple2}s. The scores of the resulting set are calculated in a way 
	 * that: from elements with lots of (highly scored) relations will have a 
	 * higher score in the resulting {@link ScoredSet} and from elements with 
	 * few (and lowly scored) relations will have a low score in teh result.   
	 *  
	 * @param minimumRelationCardinality causes this method to return an empty 
	 * 	set if none of the from elements are related to at least the 
	 * 	minimumRelationCardinality.
	 *  
	 * @return
	 */
	public ScoredSet<F> flattenToFromSet() {
//		assert(intersection(fuzzyRel.sources(), fuzzyRel.destinations()).isEmpty());
		
		ScoredSet.Builder<F> resultBuilder = ScoredSetImpl.builder();
		
		Set<T> allDestinations = getToSet();
		
		final int maxNumOfDests = maxNumberOfRelsFromASource();
		
		for (F source: getFromSet()) {
		    ScoredSet<T> destsOfSource = children(source);
		    JustificationBuilder justifBuilder = scoreFactory.newJustificationBuilder();
	        double degreeSum = 0.0;
	        for (T destination: destsOfSource) {
	        	Score relScore = destsOfSource.getScore(destination);
	            degreeSum = degreeSum+relScore.getValue();
	            justifBuilder.addSubJustification(relScore.getJustification());
	        }
	        /* FIXME: The score for the source is the sum of all the supporting
	         * relations in relSet, but it's biased towards source elements 
	         * which have a large number of relations. However, the current 
	         * implementation may achieve the opposite. Suppose for example that
	         * the relations are:
	         * S1 -> D1 1.0
	         * S1 -> D2 1.0
	         * S1 -> D3 1.0
	         * S2 -> D4 1.0
	         * 
	         * Then, just based on the sum across all destinations, you'd have
	         * S1 3/4
	         * S2 1/4
	         * I.e. score for S1 is 3x that of S2, but you want to increase 
	         * this difference between the scores. However, the current 
	         * implementation gives you:
	         * S1 3 * 3/4 = 9/4
	         * S2 1 * 1/4 = 1/4
	         * So far, so good, as the score of S1 is now 9 times as big as the 
	         * score for S2. Except that the score for S1 gets normalized (
	         * values above 1.0, are reduced to 1.0), so it will be 1 = 4/4, and 
	         * the effect is lowered again to 4 times that of the score of S2.
	         * 
	         * A better alternative would be to do a first pass to find the 
	         * maximum number of relations and use that as the factor. That way 
	         * we are guaranteed that the scoreVal will always be less than 1,
	         * (assuming that the input relation is normalized), since degreeSum
	         * will be at most equal to maxNumOfDests.
	         *  
	         */
	        justifBuilder.add("The sum of all " + destsOfSource.size() + " " 
	        		+ relMeta.getRelationName() + " from " 
	        		+ source + " (out of a total of " + allDestinations.size() 
	        		+ " " + relMeta.destinationsName() + ") was " + degreeSum);
	        final double scoreVal = degreeSum * ((double)destsOfSource.size() / allDestinations.size());
//	        final double scoreVal = degreeSum / (double)maxNumOfDests; //TODO apply fix (see problem description above)
	        log.debug("Score for " + source + " = " + scoreVal);
	        Score sourceEltDegree = scoreFactory.newScore(
	        		Math.max(0.0, Math.min(1.0, scoreVal)),
	        		justifBuilder.build());
	        resultBuilder.add(source, sourceEltDegree);
		}
		return resultBuilder.normalizeBasedOnScoreSum().build();
	}

	/**
	 * Returns the maximum number of relations from a single source in this 
	 * {@link ScoredRelSet} to destination objects.
	 *  
	 * @return
	 */
	public int maxNumberOfRelsFromASource() {
		int maxNumOfDests = 0;
		for (F source: getFromSet()) {
			maxNumOfDests = Math.max(maxNumOfDests, children(source).size());
		}
		return maxNumOfDests;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.isoco.kt.score.ScoredRelSet#getToSet()
	 */
	public final Set<T> getToSet() {
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		for (R rel: relSet) {
			builder.add(rel.getTo());
		}
		return builder.build();
	}

	public final Set<F> getFromSet() {
		ImmutableSet.Builder<F> builder = ImmutableSet.builder();
		for (R rel: relSet) {
			builder.add(rel.getFrom());
		}
		return builder.build();
	}

	/*
	 * (non-Javadoc)
	 * @see com.isoco.kt.score.ScoredRelSet#children(java.lang.Object)
	 */
	@Override
	public ScoredSet<T> children(F source) {
		ScoredSet.Builder<T> builder = ScoredSetImpl.builder();
		for (R rel: relSetFrom(source)) {
			builder.add(rel.getTo(), relSet.getScore(rel));
		}
		return builder.build();
	}
	
	/**
	 * Returns the subset of {@link #relSet} for all relations which have 
	 * <code>source</code> as their source.
	 * 
	 * @param source
	 * @return
	 */
	private Set<R> relSetFrom(F source) {
		initRelSetFromIndex();
		return relSetFromIndex.get(source);
	}

	/**
	 * Initialises {@link #relSetFromIndex} if necessary.
	 */
	private void initRelSetFromIndex() {
		if (relSetFromIndex != null) return;
		ImmutableSetMultimap.Builder<F, R> indexBuilder = ImmutableSetMultimap.builder();
		for (R rel: relSet) {
			indexBuilder.put(rel.getFrom(), rel);
		}
		relSetFromIndex = indexBuilder.build();
	}

	/*
	 * (non-Javadoc)
	 * @see com.isoco.kt.score.ScoredRelSet#parents(java.lang.Object)
	 */
	@Override
	public ScoredSet<F> parents(T destination) {
		ScoredSet.Builder<F> builder = ScoredSetImpl.builder();
		for (R rel: relSet) {
			if (rel.getTo().equals(destination)) 
				builder.add(rel.getFrom(), relSet.getScore(rel));
		}
		return builder.build();
	}

	public final ImmutableSetMultimap<F, T> asMultimap() {
		ImmutableSetMultimap.Builder<F, T> builder = ImmutableSetMultimap.builder();
		for (R rel: relSet) {
			builder.put(rel.getFrom(), rel.getTo());
		}
		return builder.build();
	}

	@Override
	public Score getScore(R object) {
		return relSet.getScore(object);
	}

	@Override
	public Set<R> unscored() {
		return relSet.unscored();
	}

	@Override
	public List<R> asList() {
		return relSet.asList();
	}


	@Override
	public int size() {
		return relSet.size();
	}

	@Override
	public boolean isEmpty() {
		return relSet.isEmpty();
	}

	@Override
	public Iterator<R> iterator() {
		return relSet.iterator();
	}


	@Override
	public Map<F, List<T>> asMap() {
		ImmutableMap.Builder<F, List<T>> builder = ImmutableMap.builder();
		for (F source: getFromSet()) {
			builder.put(source, children(source).asList());
		}
		return null;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((relMeta == null) ? 0 : relMeta.hashCode());
		result = prime * result + ((relSet == null) ? 0 : relSet.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ScoredRelSetImpl))
			return false;
		ScoredRelSetImpl other = (ScoredRelSetImpl) obj;
		if (relMeta == null) {
			if (other.relMeta != null)
				return false;
		} else if (!relMeta.equals(other.relMeta))
			return false;
		if (relSet == null) {
			if (other.relSet != null)
				return false;
		} else if (!relSet.equals(other.relSet))
			return false;
		return true;
	}
	
}
