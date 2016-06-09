package eu.xlime.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.base.Optional;

/**
 * Integration test for the {@link AutocompleteClient}.
 * 
 * @author Nuria Garcia 
 * @email ngarcia@expertsystem.com
 *
 */

public class AutocompleteClientITCase {
	
	@Test
	public void testRetrieveSummary() throws Exception {		
		AutocompleteClient client = new AutocompleteClient();
		Optional<AutocompleteBean> autocomplete = client.retrieveAutocomplete("refugee");	
		assertNotNull(autocomplete);
		System.out.println("autocomplete: " + autocomplete.toString());
		assertTrue(autocomplete.isPresent());
	}
}
