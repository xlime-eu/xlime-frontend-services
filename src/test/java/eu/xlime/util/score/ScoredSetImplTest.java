package eu.xlime.util.score;

import static org.junit.Assert.*;

import org.junit.Test;

public class ScoredSetImplTest {

	ScoreFactory scoreFactory = ScoreFactory.instance;
			
	@Test
	public void testBuilderNormalize1() throws Exception {
		ScoredSet<String> set = ScoredSetImpl.<String>builder()
				.add("M1", scoreFactory.newScore(0.5))
				.add("M2", scoreFactory.newScore(0.5))
				.add("M3", scoreFactory.newScore(2.0))
				.normalizeBasedOnScoreSum()
				.build();
		
		ScoredSet<String> expected = ScoredSetImpl.<String>builder()
				.add("M1", scoreFactory.newScore(1.0/6.0))
				.add("M2", scoreFactory.newScore(1.0/6.0))
				.add("M3", scoreFactory.newScore(2.0/3.0))
				.build();
		
		assertEquals(expected, set);
	}

	@Test
	public void testBuilderNormalize2() throws Exception {
		ScoredSet<String> set = ScoredSetImpl.<String>builder()
				.add("M1", scoreFactory.newScore(0.5))
				.add("M2", scoreFactory.newScore(0.5))
				.add("M3", scoreFactory.newScore(1.0))
				.normalizeBasedOnScoreSum()
				.build();
		
		ScoredSet<String> expected = ScoredSetImpl.<String>builder()
				.add("M1", scoreFactory.newScore(0.25))
				.add("M2", scoreFactory.newScore(0.25))
				.add("M3", scoreFactory.newScore(0.5))
				.build();
		
		assertEquals(expected, set);
	}
}
