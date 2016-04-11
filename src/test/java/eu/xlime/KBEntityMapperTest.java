package eu.xlime;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Optional;

public class KBEntityMapperTest {

	@Test
	public void testToCanonicalEntity() {
		KBEntityMapper testObj = new KBEntityMapper();
		Optional<String> optResult = testObj.toCanonicalEntityUrl("http://fr.wikipedia.org/wiki/Lünen");
		assertTrue(optResult.isPresent());
		assertEquals("http://dbpedia.org/resource/Lünen", optResult.get());
	}
	
	@Test
	public void testIsWikiEnt() {
		KBEntityMapper testObj = new KBEntityMapper();
		assertTrue(testObj.isWikiEnt("http://fr.wikipedia.org/wiki/Lünen"));
	}
	
}
