package eu.xlime.util;

import static org.junit.Assert.*;

import java.util.Date;

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
	
	@Test
	public void testParseTimeZoneAndUTC() {
		SparqlToBeanConverter testObj = createTestObj();
		
		final String annotationTime = "2016-06-13T15:56:15.500000+00:00";
		final String progStart= "2016-06-13T15:55:00Z";
		final int progDurationSec = 300;

		Date dTimezone = testObj.extractISODate(annotationTime);
		Date dUTC = testObj.extractISODate(progStart);
		Date dUTCEnd = new Date(dUTC.getTime() + (progDurationSec * 1000));
		System.out.println(String.format("Program start %s, end %s, annotation %s", dUTC, dUTCEnd, dTimezone));
		assertNotNull(dTimezone);
		assertNotNull(dUTC);
		assertTrue(dTimezone.after(dUTC));
		assertTrue(dUTCEnd.after(dTimezone));
	}

	private SparqlToBeanConverter createTestObj() {
		return new SparqlToBeanConverter();
	}
	
}
