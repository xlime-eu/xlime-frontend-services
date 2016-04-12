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
	}

	@Test
	public void testRetrieveFromUri2() {
		UIEntityFactory factory = UIEntityFactory.instance;
		UIEntity entity = factory.retrieveFromUri("http://dbpedia.org/resource/S&P_500_Index");
		System.out.println("Found entity " + entity);
		assertNotNull(entity);
	}

	@Test
	public void testRetrieveFromUri3() {
		UIEntityFactory factory = UIEntityFactory.instance;
		UIEntity entity = factory.retrieveFromUri("http://dbpedia.org/resource/Sneakers_%28footwear%29");
		System.out.println("Found entity " + entity);
		assertNotNull(entity);
		entity = factory.retrieveFromUri("http://dbpedia.org/resource/Sneakers_(footwear)");
		System.out.println("Found entity " + entity);
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
