package eu.xlime.summa;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.xlime.summa.bean.UIEntity;
import eu.xlime.summa.bean.UIEntityFactory;

public class UIEntityFactoryITCase {

	@Test
	public void testFindNewsArticle() {
		UIEntityFactory factory = UIEntityFactory.instance;
		UIEntity entity = factory.retrieveFromUri("http://dbpedia.org/resource/Berlin");
		System.out.println("Found entity " + entity);
		assertNotNull(entity);
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
