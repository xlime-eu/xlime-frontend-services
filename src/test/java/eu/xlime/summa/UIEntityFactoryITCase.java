package eu.xlime.summa;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.xlime.summa.bean.UIEntity;

public class UIEntityFactoryITCase {

	@Test
	public void testRetrieveFromUri() {
		UIEntityFactory factory = UIEntityFactory.instance;
		UIEntity entity = factory.retrieveFromUri("http://dbpedia.org/resource/Berlin");
		System.out.println("Found entity " + entity);
		assertNotNull(entity);
		assertEquals("Berlin", entity.getLabel());
		assertEquals(2, entity.getDepictions().size());
		assertTrue(entity.getTypes().contains("http://schema.org/Place"));
	}

	@Test
	public void testRetrieveFromUri2() {
		UIEntityFactory factory = UIEntityFactory.instance;
		UIEntity entity = factory.retrieveFromUri("http://dbpedia.org/resource/S&P_500_Index");
		System.out.println("Found entity " + entity);
		assertNotNull(entity);
		assertEquals("S&P 500 Index", entity.getLabel());
//		assertEquals(2, entity.getDepictions().size()); // 0 from xLiMe, 2 from dbpedia
		assertTrue(entity.getTypes().isEmpty());
	}

	@Test
	public void testRetrieveFromUri3() {
		UIEntityFactory factory = UIEntityFactory.instance;
		UIEntity entity = factory.retrieveFromUri("http://dbpedia.org/resource/Sneakers_%28footwear%29");
		System.out.println("Found entity " + entity);
		assertNotNull(entity);
		assertNull(entity.getLabel());
		assertTrue(entity.getDepictions().isEmpty());
		entity = factory.retrieveFromUri("http://dbpedia.org/resource/Sneakers_(footwear)");
		System.out.println("Found entity " + entity);
		assertEquals("Sneakers (footwear)", entity.getLabel());
		assertEquals(2, entity.getDepictions().size());
	}
	
	@Test
	public void testSummaryCache() throws Exception {
		retrieveAll("http://dbpedia.org/resource/Berlin", "http://dbpedia.org/resource/Moscow", "http://dbpedia.org/resource/Europe", "http://dbpedia.org/resource/Amsterdam",
				"http://dbpedia.org/resource/Berlin", "http://dbpedia.org/resource/Moscow", "http://dbpedia.org/resource/Europe", "http://dbpedia.org/resource/Amsterdam"
				);
		
	}

	private void retrieveAll(String... urls) {
		UIEntityFactory factory = UIEntityFactory.instance;
		for (String url: urls) {
			UIEntity entity = factory.retrieveFromUri(url);
			System.out.println("Found entity " + entity);
			assertNotNull(entity);
		}
	}
	
}
