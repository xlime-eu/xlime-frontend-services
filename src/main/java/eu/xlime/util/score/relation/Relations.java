package eu.xlime.util.score.relation;

import java.util.Map;

import eu.xlime.util.score.Score;
import eu.xlime.util.score.ScoredRelSetImpl;
import eu.xlime.util.score.ScoredSet;
import eu.xlime.util.score.Tuple2.Tuple2Metadata;

/**
 * Example implementation of a generic set of relations between instances of a 
 * source type F and instances of a destination type T.
 * 
 * @author rdenaux
 *
 * @param <F> the source type. All the {@link Relation}s in this scored set will 
 * 	have instances of this type as their source.
 * @param <T> the destination type. All the {@link Relation}s in this scored set 
 * 	will have instances of this type as their destination.
 */
public final class Relations<F, T> extends ScoredRelSetImpl<Relation<F, T>, F, T> {

	private static final long serialVersionUID = -8775634012279401705L;

	public static class RelationsBuilder<F, T> extends ScoredRelSetImplBuilder<Relation<F,T>, F, T> {

		private RelationsBuilder(Relation.Metadata<F, T> relationMetadata) {
			super(Relation.<F,T>builder(relationMetadata));
		}

		@Override
		public RelationsBuilder<F, T> add(
				F source, T destination, Score newScore) {
			super.add(source, destination, newScore);
			return this;
		}

		@Override
		public RelationsBuilder<F, T> add(
				Relation<F, T> object, Score newScore) {
			super.add(object, newScore);
			return this;
		}

		@Override
		public RelationsBuilder<F, T> addAll(
				ScoredSet<Relation<F, T>> toCopy) {
			super.addAll(toCopy);
			return this;
		}

		@Override
		public RelationsBuilder<F, T> addAll(
				Map<F, ScoredSet<T>> toCopy) {
			super.addAll(toCopy);
			return this;
		}

		@Override
		public <S extends T> RelationsBuilder<F, T> addAll(
				F source, ScoredSet<S> destinations) {
			super.addAll(source, destinations);
			return this;
		}

		@Override
		public RelationsBuilder<F, T> normalizeBasedOnScoreSum() {
			super.normalizeBasedOnScoreSum();
			return this;
		}

		@Override
		public Relations<F, T> build() {
			return new Relations(buildScoredSet(), getRelationMetadata());
		}
		
	}
	
	/**
	 * Creates and returns a new {@link RelationsBuilder} that lets you create
	 * relations between objects of type F and objects of type T. Furthermore the
	 * {@link Relation} that will be contained in the built {@link Relations} 
	 * will be distinguished by their {@link Relation.Metadata#getRelationName()}.
	 * 
	 * @param relationMetadata
	 * @return
	 */
	public static <F, T> RelationsBuilder<F,T> builder(
			Relation.Metadata<F, T> relationMetadata) {
		return new RelationsBuilder(relationMetadata);
	}
	
	protected Relations(
			ScoredSet<Relation<F, T>> delegate,
			Tuple2Metadata<Relation<F, T>, F, T> aRelationMetadata) {
		super(delegate, aRelationMetadata);
	}

}
