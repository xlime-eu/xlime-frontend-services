package eu.xlime.sparql;

import static org.junit.Assert.*;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SparqlQueryFactoryTest {

	SparqlQueryFactory qFactory = new SparqlQueryFactory();
	
	@Test
	public void testMicroPostsByKeywordFilter() throws Exception {
		String query = qFactory.microPostsByKeywordFilter(ImmutableList.of("keyword 1", "keyword 2"));
		System.out.println("query:\n" + query);
		assertTrue(query.contains("?keywordFilter = \"keyword 1\""));
	}
	
	@Test
	public void testMicroPostDetails() throws Exception {
		String q = qFactory.microPostDetails("http://example.com");
		System.out.println(q);
		assertNotNull(q);
	}
	
	@Test
	public void testNewsArticleDetails() throws Exception {
		String q = qFactory.newsArticleDetails("http://ijs.si/article/367691732");
		System.out.println(q);
		assertNotNull(q);
	}

	@Test
	public void testMediaItemUrlsByDate() throws Exception {
		Date now = new Date();
		DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss");
		String q = qFactory.mediaItemUrlsByDate(now.getTime() - (1000 * 60), now.getTime(), 10, formatter);
		System.out.println(q);
		assertNotNull(q);
	}
	
	
}
