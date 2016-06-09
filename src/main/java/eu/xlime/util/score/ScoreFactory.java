package eu.xlime.util.score;

import java.util.Arrays;

import com.google.common.base.Optional;

public class ScoreFactory {
	
	public static final ScoreFactory instance = new ScoreFactory();
	
	/**
	 * Used to create the private static instance.
	 */
	private ScoreFactory() {
		
	}
	
	private static final Optional<String> absentJustification = Optional.absent();

	public <T> Scored<T> newScored(T objectToScore, Score score) {
		return new ScoredImpl<T>(objectToScore, score);
	}
	
	public Score newScore(double value) {
		return newScore(value, absentJustification);
	}
	
	public Score newScore(double value, String justification) {
		if (justification == null || justification.trim().isEmpty())
			return newScore(value);
		return newScore(value, Optional.of(justification));
	}
	
	private Score newScore(double value, Optional<String> justification) {
		return new ScoreImpl(value, justification);
	}
	
	/**
	 * Returns a (possibly new) version of an <code>originalScored</code> object
	 * but containing the best score out of the current score in 
	 * <code>originalScored</code> and an array of <code>possibleScores</code>.
	 * 
	 * @param originalScored
	 * @param possibleScores
	 * @return
	 */
	public <T> Scored<T> bestScored(Scored<T> originalScored, Score... possibleScores) {
		Score originalScore = newScore(originalScored.getValue(), originalScored.getJustification());
		Score bestScore = best(originalScore, possibleScores);
		return newScored(originalScored.getObject(), bestScore);
	}
	
	/**
	 * Returns the best score, i.e. the score with the highest value. If two or 
	 * more scores have the highest value, a new score with that value will be
	 * returned, while the justifications will be merged.
	 * 
	 * @param s1
	 * @param ss
	 * @return
	 */
	public Score best(Score s1, Score... ss) {
		if (ss == null || ss.length == 0) return s1;
		//ss.length >= 1
		return best(bestOfPair(s1, ss[0]), Arrays.copyOfRange(ss, 1, ss.length));
	}

	private Score bestOfPair(Score s1, Score s2) {
		if (s1.getValue() > s2.getValue()) return s1;
		else if (s1.getValue() < s2.getValue()) return s2;
		else return newScore(s1.getValue(), mergeJustifications(s1, s2));
	}

	private Optional<String> mergeJustifications(Score s1, Score s2) {
		String merged = s1.getJustification().or("") + "\n" + s2.getJustification().or("");
		merged = merged.trim();
		if (merged.isEmpty()) return absentJustification;
		else return Optional.of(merged);
	}

	public JustificationBuilder newJustificationBuilder() {
		return new JustificationBuilder();
	}
}
