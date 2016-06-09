package eu.xlime.util.score;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * Default implementation of a (justified) {@link Score}.
 * 
 * @author rdenaux
 *
 */
public class ScoreImpl implements Score, Serializable {
	
	private static final long serialVersionUID = -3099777038291661351L;

	private static final Logger log = LoggerFactory.getLogger(ScoredImpl.class);

	private static final double equalityThreshold = 0.001;
	
	private double value;
	
	private Optional<String> justification;
	
	ScoreImpl(double value, Optional<String> justification) {
		super();
		this.value = value;
		if (justification == null) 
			this.justification = Optional.absent();
		else this.justification = justification;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Score [value=").append(value);
		if (justification != null && log.isTraceEnabled())
			builder.append(", ").append("justification=").append(justification);
		builder.append("]");
		return builder.toString();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Equality of {@link Score}s is based only on the {@link #getValue()} of
	 * the score, <b>not</b> on the {@link #getJustification()}s. 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ScoreImpl))
			return false;
		ScoreImpl other = (ScoreImpl) obj;
		if (!valueEquals(other.value, equalityThreshold)) return false;
//		if (Double.doubleToLongBits(value) != Double
//				.doubleToLongBits(other.value))
//			return false;
		return true;
	}


	private boolean valueEquals(double value2, double equalitythreshold2) {
		return Math.abs(value - value2) <= equalitythreshold2;
	}

	@Override
	public Double getValue() {
		return value;
	}

	@Override
	public Optional<String> getJustification() {
		return justification;
	}

}
