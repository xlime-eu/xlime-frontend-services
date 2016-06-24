package eu.xlime.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SparqlToBeanConverterTest {

	@Test
	public void testParseISODate() {
		SparqlToBeanConverter testObj = createTestObj();
		
		String dateTimezone = "2015-05-04T01:25:17+02:00";
		String dateNoTimezone = "2015-09-02T00:24:59";

		assertNotNull(testObj.extractISODate(dateTimezone));
		assertNotNull(testObj.extractISODate(dateNoTimezone));
	}

	private SparqlToBeanConverter createTestObj() {
		return new SparqlToBeanConverter();
	}
	
}
