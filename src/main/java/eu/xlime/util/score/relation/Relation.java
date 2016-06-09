package eu.xlime.util.score.relation;

import java.io.Serializable;

import eu.xlime.util.score.Tuple2;

/**
 * Generic implementation of a {@link Tuple2} denoting a directed relation between 
 * a source object of type F and a destination object of type T.
 *  
 * @author rdenaux
 *
 * @param <F>
 * @param <T>
 */
public class Relation<F, T> implements Tuple2<F, T>, Serializable {

	private static final long serialVersionUID = -4956432112605827444L;
	
	/**
	 * The source object in this {@link Relation}
	 */
	private final F from;
	/**
	 * The destination object in this {@link Relation}
	 */
	private final T to;
	/**
	 * The metadata describing this {@link Relation} type.
	 */
	private final Metadata<F, T> relationMetadata;

	/**
	 * Describes a {@link Relation} type by defining its source and destination
	 * types and giving a name to the relation type.
	 * 
	 * The {@link #getRelationName()} allows you to distinguish two 
	 * {@link Relation}s with the same source and destination type, but a 
	 * different {@link #getRelationName()}. 
	 * 
	 * @author rdenaux
	 *
	 * @param <F>
	 * @param <T>
	 */
	public static class Metadata<F, T> implements Tuple2.Tuple2Metadata<Relation<F,T>, F, T> {

		private static final long serialVersionUID = -6785938687664908825L;
		
		private final String relationName;
		private final Class<F> sourceClass;
		private final Class<T> destinationClass;
		
		public Metadata(
				String relationName, 
				Class<F> sourceClass,
				Class<T> destinationClass) {
			super();
			if (relationName == null) throw new NullPointerException("relationName");
			if (sourceClass == null) throw new NullPointerException("sourcesClass");
			if (destinationClass == null) throw new NullPointerException("destinationClass");
			this.relationName = relationName;
			this.sourceClass = sourceClass;
			this.destinationClass = destinationClass;
		}

		@Override
		public String getRelationName() {
			return relationName;
		}

		public Class<F> getSourceClass() {
			return sourceClass;
		}

		public Class<T> getDestinationClass() {
			return destinationClass;
		}

		@Override
		public String sourcesName() {
			return sourceClass.getSimpleName() + " instances";
		}

		@Override
		public String destinationsName() {
			return destinationClass.getSimpleName() + " instances";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((destinationClass == null) ? 0 : destinationClass
							.hashCode());
			result = prime * result
					+ ((relationName == null) ? 0 : relationName.hashCode());
			result = prime * result
					+ ((sourceClass == null) ? 0 : sourceClass.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Metadata))
				return false;
			Metadata other = (Metadata) obj;
			if (destinationClass == null) {
				if (other.destinationClass != null)
					return false;
			} else if (!destinationClass.equals(other.destinationClass))
				return false;
			if (relationName == null) {
				if (other.relationName != null)
					return false;
			} else if (!relationName.equals(other.relationName))
				return false;
			if (sourceClass == null) {
				if (other.sourceClass != null)
					return false;
			} else if (!sourceClass.equals(other.sourceClass))
				return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Metadata [");
			if (relationName != null)
				builder.append("relationName=").append(relationName)
						.append(", ");
			if (sourceClass != null)
				builder.append("sourceClass=").append(sourceClass).append(", ");
			if (destinationClass != null)
				builder.append("destinationClass=").append(destinationClass);
			builder.append("]");
			return builder.toString();
		}
	
		
	}
	public static <F, T> Tuple2.Builder<Relation<F, T>, F, T> builder(
			final Metadata<F,T> relationMetadata) {
		return new Tuple2.Builder<Relation<F,T>, F, T>() {
			@Override
			public Relation<F, T> buildTuple2(F fromObject, T toObject) {
				return new Relation(fromObject, toObject, relationMetadata);
			}

			@Override
			public Relation.Metadata<F,T> relationMetadata() {
				return relationMetadata;
			}
		};
	}
	
	/**
	 * Private constructor for {@link Relation}s. Use the 
	 * {@link Relation#builder()} method to construct new instances.
	 * 
	 * @param from
	 * @param to
	 * @param relationMetadata
	 */
	private Relation(
			F from, 
			T to, 
			Metadata<F,T> relationMetadata) {
		super();
		this.from = from;
		this.to = to;
		this.relationMetadata = relationMetadata;
	}

	@Override
	public F getFrom() {
		return from;
	}

	@Override
	public T getTo() {
		return to;
	}

	public Metadata<F,T> getRelationMetadata() {
		return relationMetadata;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime
				* result
				+ ((relationMetadata == null) ? 0 : relationMetadata.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Relation))
			return false;
		Relation other = (Relation) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (relationMetadata == null) {
			if (other.relationMetadata != null)
				return false;
		} else if (!relationMetadata.equals(other.relationMetadata))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Relation (");
		if (relationMetadata != null)
			builder.append(relationMetadata.relationName);
		builder.append(") [");
		if (from != null)
			builder.append(from).append(" -> ");
		if (to != null)
			builder.append(to);
		builder.append("]");
		return builder.toString();
	}
	
	
}
