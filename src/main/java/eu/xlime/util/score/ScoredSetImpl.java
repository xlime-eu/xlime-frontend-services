package eu.xlime.util.score;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Default implementation for the {@link ScoredSet} interface. 
 * It uses an {@link ImmutableMap} to keep track of the objects and their 
 * scores.
 * 
 * @author rdenaux
 *
 * @param <T>
 */
public class ScoredSetImpl<T> implements ScoredSet<T>, Serializable {

	private static final long serialVersionUID = 4547895658419190945L;

	private static final Logger log = LoggerFactory.getLogger(ScoredSetImpl.class);
	
	private final static ScoreFactory scoreFactory = ScoreFactory.instance;
	
	private final ImmutableMap<T, Score> scoredEntries;

	public static <T> Builder<T> builder() {
		return new ScoredSetImplBuilder<T>();
	}
	
	public static <X> ScoredSet<X> empty() {
		Builder<X> builder = builder();
		return builder.build();
	}
	
	/**
	 * Default constructor for {@link ScoredSetImpl} instances. Direct use of
	 * this constructor is discouraged. Instead use the {@link ScoredSetImplBuilder}
	 * or extend it.
	 * 
	 * @param scoredEntries
	 */
	protected ScoredSetImpl(
			ImmutableMap<T, Score> scoredEntries) {
		super();
		this.scoredEntries = scoredEntries;
	}
	
	protected static class ScoredSetImplBuilder<T> implements ScoredSet.Builder<T> {
		Map<T, Score> mapBuilder = new HashMap<T, Score>();
		
		/**
		 * Adds an object to this ScoredSet {@link Builder} with a given score.
		 * If this {@link Builder} already contains the given object, it will be
		 * kept. However, only the best score will be kept. If you add the same
		 * object twice with the same score, their justifications will be merged.
		 *   
		 * @param object
		 * @param newScore
		 */
		public Builder add(T object, Score newScore) {
			if (object == null) throw new NullPointerException("object");
			if (newScore == null) throw new NullPointerException("newScore");
			T key = object;
			final Score existingScore = mapBuilder.get(key);
			if (existingScore == null) {
				mapBuilder.put(key, newScore);	
			} else {
				mapBuilder.put(key, scoreFactory.best(existingScore, newScore));
			}
			return this;
		}
		
		/**
		 * Similar to {@link #add(Object, Score)}, however, in this case only 
		 * the new score is kept. A warning may be issued if 
		 * @param object
		 * @param newScore
		 * @return
		 */
		public Builder replace(T object, Score newScore) {
			if (object == null) throw new NullPointerException("object");
			if (newScore == null) throw new NullPointerException("newScore");
			T key = object;
			final Score existingScore = mapBuilder.get(key);
			if (existingScore == null) {
				mapBuilder.put(key, newScore);
			} else {
				if (existingScore.getValue() > newScore.getValue())
					log.warn("Replacing a better score with a lower score: "
							+ "\n\t" + existingScore + " \n\t " + newScore);
				mapBuilder.put(key, newScore);
			}
			return this;
		}
		
		/**
		 * Adds all the object-score tuples from toCopy to this builder, following 
		 * the {@link #add(Object, Score)} semantics for merging the scores for 
		 * pre-existing objects in this {@link Builder}.
		 * 
		 * @param toCopy
		 * @return
		 */
		public Builder addAll(ScoredSet<T> toCopy) {
			for (T obj: toCopy) {
				Score score = toCopy.getScore(obj);
				add(obj, score);
			}
			return this;
		}
		
		
		@Override
		public Builder<T> replaceAll(ScoredSet<T> toCopy) {
			for (T obj: toCopy) {
				Score score = toCopy.getScore(obj);
				replace(obj, score);
			}
			return this;
		}

		/**
		 * Makes sure the score values in this builder are between 0.0 and 1.0 
		 * by dividing any values by the scoreSum. Furthermore, the sum of all
		 * the scores will be equal to 1.0. This means, in practice the
		 * individual values will be much lower than 1.0, for sets with a lot 
		 * of values.
		 * 
		 * @return
		 */
		public Builder<T> normalizeBasedOnScoreSum() {
			final double scoreSum = calcScoreSum();
	        if (scoreSum == 0) return this;
	        for (T key: mapBuilder.keySet()) {
	        	Score score = mapBuilder.get(key);
	        	Score newScore = scoreFactory.newScore(
	        			score.getValue() / scoreSum, 
	        			score.getJustification().orNull());
	        	mapBuilder.put(key, newScore);
	        }
	        return this;
		}
		
		/**
		 * Returns the sum of all the score values in this {@link Builder}, or 
		 * 0.0 if this Builder is empty.
		 * 
		 * @return
		 */
		private double calcScoreSum() {
			double scoreSum = 0.0;
	        for (T key : mapBuilder.keySet()) {
	            Score score = mapBuilder.get(key);
	            scoreSum = scoreSum + score.getValue();
	        }
			return scoreSum;
		}

		/**
		 * Returns the maximum of all the score values in this {@link Builder}, or 
		 * 0.0 if this Builder is empty.
		 * 
		 * @return
		 */
		private double calcMaxScore() {
			double maxSum = 0.0;
	        for (T key : mapBuilder.keySet()) {
	            Score score = mapBuilder.get(key);
	            maxSum = Math.max(maxSum, score.getValue());
	        }
			return maxSum;
		}
		
		public ScoredSet<T> build() {
			return buildScoredSet(); 
		}

		protected final ScoredSet<T> buildScoredSet() {
			return new ScoredSetImpl<T>(builtMap());
		}

		public ImmutableMap<T, Score> builtMap() {
			return ImmutableMap.copyOf(mapBuilder);
		}
		
		/**
		 * Convenience method for pruning objects from this Builder by 
		 * discarding those objects for which a discardCondition applies.
		 *  
		 * @param discardCondition
		 */
		protected final void discard(Predicate<T> discardCondition) {
			for (T obj: ImmutableSet.copyOf(mapBuilder.keySet())) {
				if (discardCondition.apply(obj)) mapBuilder.remove(obj); 
			}
		}
	}
	
	/**
	 * Provides access to the underlying scored entries.
	 * 
	 * @return
	 */
	public ImmutableMap<T, Score> getScoredEntries() {
		return scoredEntries;
	}

	@Override
	public Score getScore(T object) {
		return scoredEntries.get(object);
	}

	@Override	
	public Set<T> unscored() {
		return scoredEntries.keySet();
	}

	@Override
	public List<T> asList() {
		return scoredEntries.keySet().asList();
	}
	
	@Override
	public final Iterator<T> iterator() {
		return scoredEntries.keySet().iterator();
	}
	
	public int size() {
		return scoredEntries.keySet().size();
	}
	
	@Override
	public boolean isEmpty() {
		return scoredEntries.keySet().isEmpty();
	}

	@Override
	public String toString() {
		final int maxLen = 15;
		StringBuilder sb = new StringBuilder();
		sb.append("ScoredSet [");
		if (scoredEntries != null) {
			int i = 0;
			for (T obj: scoredEntries.keySet()) {
				if (i > maxLen) {
					sb.append("... and "  
						+ (scoredEntries.size() - maxLen) + " more..");
					break;
				}
				if (i > 0) sb.append(", ");
				sb.append(obj).append(" -> ").append(getScore(obj));
				i++;
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((scoredEntries == null) ? 0 : scoredEntries.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ScoredSetImpl))
			return false;
		ScoredSetImpl other = (ScoredSetImpl) obj;
		if (scoredEntries == null) {
			if (other.scoredEntries != null)
				return false;
		} else if (!scoredEntries.equals(other.scoredEntries))
			return false;
		return true;
	}
	
}
