package eu.xlime.summa;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Optional;

import eu.xlime.summa.bean.EntitySummary;

/**
 * Integration test for the {@link SummaClient}. Requires a configured summa server.
 * 
 * @author RDENAUX
 *
 */
public class SummaClientITCase {

	@Test
	public void testRetrieveSummary() throws Exception {
		SummaClient client = new SummaClient();
		Optional<EntitySummary> summary = client.retrieveSummary("http://dbpedia.org/resource/Berlin");
		assertNotNull(summary);
		System.out.println("summary: " + summary);
		assertTrue(summary.isPresent());
	}
	
}
