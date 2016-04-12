package eu.xlime.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.xlime.util.KBEntityMapper;

public class KBEntityMapperTest {

	
	@Test
	public void testIsWikiEnt() {
		KBEntityMapper testObj = new KBEntityMapper();
		assertTrue(testObj.isWikiEnt("http://fr.wikipedia.org/wiki/Lünen"));
	}
	
	@Test
	public void testIsLangDependentDBpediaEnt() {
		KBEntityMapper testObj = new KBEntityMapper();
		assertTrue(testObj.isLangDependentDBpediaEntity("http://es.dbpedia.org/resource/Alemania"));
	}
	
	@Test
	public void testDecodedDBpedia() {
		KBEntityMapper testObj = new KBEntityMapper();
		assertTrue(testObj.isEncodedUrl("http://dbpedia.org/resource/RT_%28TV_network%29"));
		
	}
	
}
