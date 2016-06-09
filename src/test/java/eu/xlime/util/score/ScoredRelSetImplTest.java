package eu.xlime.util.score;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.xlime.util.score.relation.Relation;
import eu.xlime.util.score.relation.Relations;

public class ScoredRelSetImplTest {

	ScoreFactory scoreFactory = ScoreFactory.instance;
	
	@Test
	public void testFlattenToFromSet() throws Exception {
		Relation.Metadata<String, String> relMeta = new Relation.Metadata(
				"test relations", String.class, String.class);
		
		Relations<String, String> relSet = 
				Relations.<String, String>builder(relMeta)
				  .add("M1", "T1", scoreFactory.newScore(1.0))
				  .add("M2", "T1", scoreFactory.newScore(1.0))
				  .add("M3", "T1", scoreFactory.newScore(1.0))
				  .add("M3", "T2", scoreFactory.newScore(1.0))
				  .build();

		ScoredSet<String> result = relSet.flattenToFromSet();
		
		ScoredSet<String> expected = ScoredSetImpl.<String>builder()
			.add("M1", scoreFactory.newScore(0.25))
			.add("M2", scoreFactory.newScore(0.25))
			.add("M3", scoreFactory.newScore(0.5))
			.build();
		
		assertEquals(expected, result);
	}
}
