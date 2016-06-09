package eu.xlime.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaItemImplDaoTest {

	private static final Logger log = LoggerFactory.getLogger(MediaItemImplDaoTest.class);
	
	@Test
	public void testParseISODate() {
		MediaItemDaoImpl testObj = new MediaItemDaoImpl();
		
		String dateTimezone = "2015-05-04T01:25:17+02:00";
		String dateNoTimezone = "2015-09-02T00:24:59";

		assertNotNull(testObj.extractISODate(dateTimezone));
		assertNotNull(testObj.extractISODate(dateNoTimezone));
	}
	
	@Test
	public void testExtractMainDomain() {
		MediaItemDaoImpl testObj = new MediaItemDaoImpl();

		assertEquals("twitter.com", testObj.extractMainDomain("http://www.twitter.com"));
		assertEquals("wordpress.com", testObj.extractMainDomain("http://jjjameson65.wordpress.com"));
		assertEquals("clubsearay.com", testObj.extractMainDomain("http://clubsearay.com/forum/index.php"));
	}
	
	@Test
	public void testCreatorUrlToLabel() {
		MediaItemDaoImpl testObj = new MediaItemDaoImpl();

		assertEquals("Chris Nowell", testObj.creatorUrlToLabel("http://clubsearay.com/forum/index.php#Chris%20Nowell"));
		assertEquals("YummyDestiny", testObj.creatorUrlToLabel("http://twitter.com/YummyDestiny")); 
		assertEquals("Google+ user", testObj.creatorUrlToLabel("https://plus.google.com/117956042043423124419"));
		assertEquals("jjjameson65", testObj.creatorUrlToLabel("http://jjjameson65.wordpress.com#jjjameson65")); 
		assertEquals("Daniel Cosenza", testObj.creatorUrlToLabel("https://www.facebook.com#Daniel%20Cosenza")); 
	}
	
}
