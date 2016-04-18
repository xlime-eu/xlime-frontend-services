package eu.xlime.search;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Optional;

/**
 * Integration test for the {@link SearchItemDao}.
 * 
 * @author Nuria Garcia 
 * @email ngarcia@expertsystem.com
 *
 */

public class SearchItemDaoCase {

	@Test
	public void testFindMediaItemFromText() {
		SearchItemDao search = new SearchItemDao();
		Optional<List<String>> bean = search.findMediaItemUrlsByText("refugee");
		System.out.println("Found media items " + bean.toString());
		assertTrue(bean.isPresent());
	}

	@Test
	public void testFindMediaItemFromEntity() {
		SearchItemDao search = new SearchItemDao();
		Optional<List<String>> bean = search.findMediaItemUrlsByKBEntity("http://dbpedia.org/resource/Refugee");
		System.out.println("Found media items " + bean.toString());
		assertTrue(bean.isPresent());
	}
	
	@Test
	public void testFindMediaItemFromFreeText() {
		SearchItemDao search = new SearchItemDao();
		Optional<List<String>> bean = search.findMediaItemUrlsByFreeText("refugee Greece");
		System.out.println("Found media items " + bean.toString());
		assertTrue(bean.isPresent());
	}
}
