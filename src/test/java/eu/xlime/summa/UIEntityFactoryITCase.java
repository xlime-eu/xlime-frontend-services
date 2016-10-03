package eu.xlime.summa;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Optional;

import eu.xlime.dao.UIEntityDao;
import eu.xlime.dao.entity.UIEntityDaoImpl;
import eu.xlime.summa.bean.UIEntity;

public class UIEntityFactoryITCase {

	
	//TODO: since ITcase requires MongoDB with UIEntity collection, create test mongo db with these values that can be loaded during test setup 
	
	@Test
	public void testRetrieveFromUri() {
		UIEntityDao factory = UIEntityDaoImpl.instance;
		Optional<UIEntity> entity = factory.retrieveFromUri("http://dbpedia.org/resource/Berlin");
		System.out.println("Found entity " + entity);
		assertTrue(entity.isPresent());
		assertEquals("Berlin", entity.get().getLabel());
		assertEquals(2, entity.get().getDepictions().size());
		assertTrue(entity.get().getTypes().contains("http://schema.org/Place"));
	}

	@Test
	@Ignore("Not in mongodb? or issue with url escaping?")
	public void testRetrieveFromUri2() {
		UIEntityDao factory = UIEntityDaoImpl.instance;
		Optional<UIEntity> optEntity = factory.retrieveFromUri("http://dbpedia.org/resource/S&P_500_Index");
		System.out.println("Found entity " + optEntity);
		assertTrue(optEntity.isPresent());
		UIEntity entity = optEntity.get();
		assertEquals("S&P 500 Index", entity.getLabel());
//		assertEquals(2, entity.getDepictions().size()); // 0 from xLiMe, 2 from dbpedia
		assertTrue(entity.getTypes().isEmpty());
	}

	@Test
	public void testRetrieveFromUri3() {
		UIEntityDao factory = UIEntityDaoImpl.instance;
		String uri = "http://dbpedia.org/resource/Sneakers_%28footwear%29";
		String iri = "http://dbpedia.org/resource/Sneakers_(footwear)";
		Optional<UIEntity> optEntity = factory.retrieveFromUri(uri);
		assertFalse(optEntity.isPresent()); //not present because not an IRI encoded, need to decode:
		optEntity = factory.retrieveFromUri(iri);
		System.out.println("Found entity " + optEntity);
		UIEntity entity = optEntity.get();
//		assertNotNull(entity);
//		assertNull(entity.getLabel());
//		assertTrue(entity.getDepictions().isEmpty());
//		entity = factory.retrieveFromUri("http://dbpedia.org/resource/Sneakers_(footwear)").get();
//		System.out.println("Found entity " + entity);
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
		UIEntityDao factory = UIEntityDaoImpl.instance;
		for (String url: urls) {
			UIEntity entity = factory.retrieveFromUri(url).get();
			System.out.println("Found entity " + entity);
			assertNotNull(entity);
		}
	}
	
}
