package eu.xlime.search;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import eu.xlime.util.score.ScoredSet;

/**
 * Integration test for the {@link SearchItemDao}.
 * 
 * @author Nuria Garcia 
 * @email ngarcia@expertsystem.com
 *
 */

public class SearchItemDaoITCase {

	@Test
	public void testFindMediaItemFromText() {
		SearchItemDao search = new SearchItemDao();
		ScoredSet<String> bean = search.findMediaItemUrlsByText("refugee");
		System.out.println("Found media items " + bean.toString());
		assertTrue(!bean.isEmpty());
	}

	@Test
	public void testFindMediaItemFromText2() {
		SearchItemDao search = new SearchItemDao();
		ScoredSet<String> bean = search.findMediaItemUrlsByText("Brexit");
		System.out.println("Found media items " + bean.toString());
		for (String miUrl: bean) {
			if (miUrl.contains("zattoo")) {
				System.out.println("Download? " + miUrl);
			}
		}
		assertTrue(!bean.isEmpty());
	}
	
	@Test
	public void testFindMediaItemFromEntity() {
		SearchItemDao search = new SearchItemDao();
		ScoredSet<String> itUrl = search.findMediaItemUrlsByKBEntity("http://dbpedia.org/resource/Refugee");
		System.out.println("Found media items " + itUrl.toString());
		assertTrue(!itUrl.isEmpty());
	}
	
	@Test
	public void testRetrieveMediaItemUrlsFromFreeText() {
		SearchItemDao search = new SearchItemDao();
		ScoredSet<String> itUrl = search.retrieveMediaItemUrlsFromFreeText("refugee Greece");
		System.out.println("Found media items " + itUrl.toString());
		assertTrue(!itUrl.isEmpty());
	}
	
	@Test
	public void testRetrieveMediaItemFromEntity() {
		SearchItemDao search = new SearchItemDao();
		ScoredSet<String> itUrl = search.retrieveMediaItemUrlsFromEntity("http://dbpedia.org/resource/Refugee");
		System.out.println("Found media items " + itUrl.toString());
		assertTrue(!itUrl.isEmpty());
	}
	
	@Test
	public void testRetrieveMediaItemFromSpanishEntity() {
		SearchItemDao search = new SearchItemDao();
		ScoredSet<String> itUrl = search.retrieveMediaItemUrlsFromEntity("http://es.dbpedia.org/resource/Refugiado");
		System.out.println("Found media items " + itUrl.toString());
		assertTrue(!itUrl.isEmpty());
	}
	
	@Test @Ignore("query times out using default timeout of 5s.b")
	public void testRetrieveMediaItemFromFrenchWikiEntity() {
		SearchItemDao search = new SearchItemDao();
		ScoredSet<String> itUrl = search.retrieveMediaItemUrlsFromEntity("http://fr.wikipedia.org/wiki/Lünen");
		System.out.println("Found media items " + itUrl.toString());
		assertTrue(!itUrl.isEmpty());
	}
	
}
