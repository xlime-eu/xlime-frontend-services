package eu.xlime.util.score;

import java.io.Serializable;

import com.google.common.base.Optional;

public class ScoredImpl<T> implements Scored<T>, Serializable {

	private static final long serialVersionUID = -5899455804059399020L;

	private T object;
	
	private Score score;
	
	/**
	 * Creates a {@link Scored} version of an <code>object</code> with the 
	 * specified <code>score</code>.
	 *   
	 * @param object must not be null (this will be checked) and should be an 
	 * 	immutable object (this cannot be checked).
	 * @param score must not be null.
	 */
	ScoredImpl(T object, Score score) {
		super();
		if (object == null) throw new NullPointerException("object");
		if (score == null) throw new NullPointerException("score");
		this.object = object;
		this.score = score;
	}

	@Override
	public Double getValue() {
		return score.getValue();
	}

	@Override
	public Optional<String> getJustification() {
		return score.getJustification();
	}

	@Override
	public T getObject() {
		return object;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((score == null) ? 0 : score.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ScoredImpl))
			return false;
		ScoredImpl other = (ScoredImpl) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (score == null) {
			if (other.score != null)
				return false;
		} else if (!score.equals(other.score))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Scored [");
		if (object != null)
			builder.append("object=").append(object).append(", ");
		if (score != null)
			builder.append("score=").append(score);
		builder.append("]");
		return builder.toString();
	}

	
}
