package eu.xlime.dao.mediaitem;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.sparql.SparqlClient;

public class SparqlMediaItemDaoTest {

	private static final Logger log = LoggerFactory.getLogger(SparqlMediaItemDaoTest.class);
	
	@Test
	public void testExtractMainDomain() {
		SparqlMediaItemDao testObj = createTestObj();

		assertEquals("twitter.com", testObj.extractMainDomain("http://www.twitter.com"));
		assertEquals("wordpress.com", testObj.extractMainDomain("http://jjjameson65.wordpress.com"));
		assertEquals("clubsearay.com", testObj.extractMainDomain("http://clubsearay.com/forum/index.php"));
	}
	
	@Test
	public void testCreatorUrlToLabel() {
		SparqlMediaItemDao testObj = createTestObj();

		assertEquals("Chris Nowell", testObj.creatorUrlToLabel("http://clubsearay.com/forum/index.php#Chris%20Nowell"));
		assertEquals("YummyDestiny", testObj.creatorUrlToLabel("http://twitter.com/YummyDestiny")); 
		assertEquals("Google+ user", testObj.creatorUrlToLabel("https://plus.google.com/117956042043423124419"));
		assertEquals("jjjameson65", testObj.creatorUrlToLabel("http://jjjameson65.wordpress.com#jjjameson65")); 
		assertEquals("Daniel Cosenza", testObj.creatorUrlToLabel("https://www.facebook.com#Daniel%20Cosenza")); 
	}
	
	SparqlMediaItemDao createTestObj() {
		return new MockSparqlMediaItemDaoImpl();
	}
	
	static class MockSparqlMediaItemDaoImpl extends SparqlMediaItemDao {

		@Override
		public List<NewsArticleBean> findNewsArticles(List<String> uris) {
			return ImmutableList.of();
		}

		@Override
		public List<MicroPostBean> findMicroPosts(List<String> uris) {
			return ImmutableList.of();
		}

		@Override
		public List<TVProgramBean> findTVPrograms(List<String> uris) {
			return ImmutableList.of();
		}

		@Override
		public Optional<VideoSegment> findVideoSegment(String uri) {
			return Optional.absent();
		}

		@Override
		protected SparqlClient getXLiMeSparqlClient() {
			return null;
		}
		
	}
	
}
