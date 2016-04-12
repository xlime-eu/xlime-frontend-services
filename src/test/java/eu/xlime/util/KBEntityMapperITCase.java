package eu.xlime.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Optional;

import eu.xlime.util.KBEntityMapper;

public class KBEntityMapperITCase {

	@Test
	public void testToCanonicalEntity() {
		KBEntityMapper testObj = new KBEntityMapper();
		Optional<String> optResult = testObj.toCanonicalEntityUrl("http://fr.wikipedia.org/wiki/Lünen");
		assertTrue(optResult.isPresent());
		assertEquals("http://dbpedia.org/resource/Lünen", optResult.get());
	}

	@Test
	public void testToCanonicalEntity2() {
		KBEntityMapper testObj = new KBEntityMapper();
		Optional<String> optResult = testObj.toCanonicalEntityUrl("http://es.dbpedia.org/resource/Alemania");
		assertTrue(optResult.isPresent());
		assertEquals("http://dbpedia.org/resource/Germany", optResult.get());
	}
	
}
