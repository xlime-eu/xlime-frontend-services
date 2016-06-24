package eu.xlime.util;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Optional;

public class FstKBEntityMapperTest {

	@Test 
	public void testLoad10K() throws Exception {
		File mappingFile = new File("src/test/resources/interlang_links_en_10K.ttl");
		File supportedOtherUriFile = new File("src/test/resources/supportedOtherDomains.txt"); 
		long start = System.currentTimeMillis();
		FstKBEntityMapper mapper = new FstKBEntityMapper(mappingFile, supportedOtherUriFile);
		System.out.println(String.format("Loaded %s in %s ms", mappingFile.getAbsoluteFile(), (System.currentTimeMillis() - start)));
		
		Set<String> sameAsSet = mapper.expandSameAs("http://dbpedia.org/resource/Africa");
		assertTrue("sameAsSet " + sameAsSet, sameAsSet.contains("http://es.dbpedia.org/resource/África"));
		
		Optional<String> canon = mapper.toCanonicalEntityUrl("http://eu.dbpedia.org/resource/Eskozia");
		assertFalse(canon.isPresent());
//		assertEquals("http://dbpedia.org/resource/Scotland", canon.get());
	}

	/*
	@Test 
	public void testLoad100K() throws Exception {
		File mappingFile = new File("src/test/resources/interlang_links_en_100K.ttl");
		File supportedOtherUriFile = new File("src/test/resources/supportedOtherDomains.txt"); 
		long start = System.currentTimeMillis();
		FstKBEntityMapper mapper = new FstKBEntityMapper(mappingFile, supportedOtherUriFile);
		System.out.println(String.format("Loaded %s in %s ms", mappingFile.getAbsoluteFile(), (System.currentTimeMillis() - start)));
		
		Set<String> sameAsSet = mapper.expandSameAs("http://dbpedia.org/resource/Africa");
		assertTrue(sameAsSet.contains("http://es.dbpedia.org/resource/África"));
		
		Optional<String> canon = mapper.toCanonicalEntityUrl("http://eu.dbpedia.org/resource/Eskozia");
		assertFalse(canon.isPresent());
//		assertEquals("http://dbpedia.org/resource/Scotland", canon.get());
	}
	
	@Test
	public void testLoad1M() throws Exception {
		File mappingFile = new File("src/test/resources/interlang_links_en_1M.ttl");
		File supportedOtherUriFile = new File("src/test/resources/supportedOtherDomains.txt"); 
		long start = System.currentTimeMillis();
		FstKBEntityMapper mapper = new FstKBEntityMapper(mappingFile, supportedOtherUriFile);
		System.out.println(String.format("Loaded %s in %s ms", mappingFile.getAbsoluteFile(), (System.currentTimeMillis() - start)));
		
		Set<String> sameAsSet = mapper.expandSameAs("http://dbpedia.org/resource/Africa");
		assertTrue(sameAsSet.contains("http://es.dbpedia.org/resource/África"));
		
		Optional<String> canon = mapper.toCanonicalEntityUrl("http://eu.dbpedia.org/resource/Eskozia");
		assertFalse(canon.isPresent());
//		assertEquals("http://dbpedia.org/resource/Scotland", canon.get());
	}

	@Test
	public void testLoadFull() throws Exception {
		File mappingFile = new File("D:/ont/dbpedia-2015-10/interlanguage_links_en.ttl");
		File supportedOtherUriFile = new File("src/test/resources/supportedOtherDomains.txt"); 
		long start = System.currentTimeMillis();
		FstKBEntityMapper mapper = new FstKBEntityMapper(mappingFile, supportedOtherUriFile);
		System.out.println(String.format("Loaded %s in %s ms", mappingFile.getAbsoluteFile(), (System.currentTimeMillis() - start)));
		
		Set<String> sameAsSet = mapper.expandSameAs("http://dbpedia.org/resource/Africa");
		assertTrue(sameAsSet.contains("http://es.dbpedia.org/resource/África"));
		
		Optional<String> canon = mapper.toCanonicalEntityUrl("http://es.dbpedia.org/resource/Escocia");
		assertTrue(canon.isPresent());
//		assertEquals("http://dbpedia.org/resource/Scotland", canon.get());
	}
	*/
	
}
