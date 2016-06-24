package eu.xlime.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class KBEntityUriTest {

	
	@Test
	public void testIsWikiEnt() {
		assertTrue(KBEntityUri.of("http://fr.wikipedia.org/wiki/Lünen").isWikiEnt());
	}
	
	@Test
	public void testIsLangDependentDBpediaEnt() {
		assertTrue(KBEntityUri.of("http://es.dbpedia.org/resource/Alemania").isLangDependentDBpediaEntity());
	}
	
	@Test
	public void testIsEncodedDecodedDBpedia() {
		assertTrue(KBEntityUri.of("http://dbpedia.org/resource/RT_%28TV_network%29").isEncoded());
	}

	@Test
	public void testIsDecodeUrlDBpedia() {
		assertEquals("http://dbpedia.org/resource/RT_(TV_network)", KBEntityUri.of("http://dbpedia.org/resource/RT_%28TV_network%29").decodedUrl());
	}
	
	@Test
	public void testAsIri() {
		assertEquals("http://dbpedia.org/resource/RT_(TV_network)", KBEntityUri.of("http://dbpedia.org/resource/RT_%28TV_network%29").asIri());
	}
	
	
}
