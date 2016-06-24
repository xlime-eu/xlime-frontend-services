package eu.xlime.sparql;

import static org.junit.Assert.*;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

public class SparqlQueryFactoryTest {

	SparqlQueryFactory qFactory = new SparqlQueryFactory();
	
	@Test
	public void testDBpediaUIEntity() throws Exception {
		String q = qFactory.dbpediaUIEntity(ImmutableList.of("http://example.com"), "es");
		assertIsValidSelect(q);
	}
	
	
	@Test
	public void testMicroPostEntityAnnotations() throws Exception {
		String q = qFactory.microPostEntityAnnotations("http://example.com");
		assertIsValidSelect(q);
	}
	
	@Test
	public void testNewsArticleEntityAnnotations() throws Exception {
		String q = qFactory.newsArticleEntityAnnotations("http://example.com");
		assertIsValidSelect(q);
	}
	
	@Test @Ignore("jena doesn't support syntax for property path ranges? (prop){,maxHops} ")
	public void testSameAs() throws Exception {
		String q = qFactory.sameAs("http://example.com");
		System.out.println("sameas q\n" + q);
		assertIsValidSelect(q);
	}
	
	@Test
	public void testDBpediaSameAsPrimaryTopic() throws Exception {
		String q = qFactory.dbpediaSameAsPrimaryTopicOf("http://example.com");
		assertIsValidSelect(q);
	}
	
	@Test
	public void testMicroPostsByKeywordFilter() throws Exception {
		String query = qFactory.microPostsByKeywordFilter(ImmutableList.of("keyword 1", "keyword 2"));
		System.out.println("query:\n" + query);
		assertIsValidSelect(query);
		assertTrue(query.contains("?keywordFilter = \"keyword 1\""));
	}
	
	@Test
	public void testMicroPostDetailsUrl() throws Exception {
		String q = qFactory.microPostDetails("http://example.com");
		System.out.println(q);
		assertIsValidSelect(q);
		assertNotNull(q);
	}

	@Test
	public void testMicroPostDetailsList() throws Exception {
		String q = qFactory.microPostDetails(ImmutableList.of("http://example.com", "http://example.com/2"));
		assertIsValidSelect(q);
		assertNotNull(q);
	}
	
	@Test
	public void testSiocName() throws Exception {
		String q = qFactory.siocNameOf("http://example.com");
		assertIsValidSelect(q);
	}

	@Test
	public void testLabelOf() throws Exception {
		String q = qFactory.labelOf("http://example.com");
		assertIsValidSelect(q);
	}
	
	
	@Test
	public void testNewsArticleDetailsUrl() throws Exception {
		String q = qFactory.newsArticleDetails("http://ijs.si/article/367691732");
		System.out.println(q);
		assertIsValidSelect(q);
		assertNotNull(q);
	}

	@Test
	public void testNewsArticleDetailsList() throws Exception {
		String q = qFactory.newsArticleDetails(ImmutableList.of("http://ijs.si/article/367691732", "http://example.com"));
		System.out.println(q);
		assertIsValidSelect(q);
		assertNotNull(q);
	}
	
	@Test
	public void testMediaItemUrlsByDate() throws Exception {
		Date now = new Date();
		DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss");
		String q = qFactory.mediaItemUrlsByDate(now.getTime() - (1000 * 60), now.getTime(), 10, formatter);
		assertIsValidSelect(q);
		System.out.println(q);
		assertNotNull(q);
	}
	
	@Test
	public void testMediaResourceUrl() throws Exception {
		String q = qFactory.mediaResource("http://example.com");
		assertIsValidSelect(q);
	}

	@Test
	public void testMediaResourceList() throws Exception {
		String q = qFactory.mediaResource(ImmutableList.of("http://example.com", "http://example.com/2"));
		assertIsValidSelect(q);
	}
	
	@Test
	public void testMediaItemUrls() throws Exception {
		String q = qFactory.mediaItemUrls();
		assertIsValidSelect(q);
	}
	
	@Test
	public void testMediaItemUrlsOptional() throws Exception {
		String q = qFactory.mediaItemUrls(Optional.of(5));
		assertIsValidSelect(q);
	}
	
	@Test
	public void testMediaResourceOCRAnnotations() throws Exception {
		String q = qFactory.mediaResourceOCRAnnotations("http://example.com");
		assertIsValidSelect(q);
	}
	
	@Test
	public void testEntityAnnotationInMediaItem() throws Exception {
		String q = qFactory.entityAnnotationInMediaItem(ImmutableSet.of("http://example.com"), 0.9);
		assertIsValidSelect(q);
	}
	
	@Test
	public void testMediaItemUrisBySource() throws Exception {
		String q = qFactory.mediaItemUrisBySource(ImmutableList.of("http://example.com", "http://example.com/2"));
		assertIsValidSelect(q);
	}
	
	private void assertIsValidSelect(String sparql) {
		Query q = QueryFactory.create(sparql, Syntax.syntaxSPARQL_11);
		assertTrue(q.isSelectType());
	}
	
	
}
