package eu.xlime.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.google.common.base.Optional;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.dao.annotation.MediaItemAnnotationDaoFromDatasetTest;
import eu.xlime.dao.mediaitem.MediaItemDaoFromDatasetTest;
import eu.xlime.mongo.ConfigOptions;

public class MongoXLiMeResourceStorerTest {

	@Test
	public void testUpsertAndRetrieveMicroPost() {
		List<MicroPostBean> resourcesToInsert = getTestMicroPosts();
		testUpsertAndRetrieve(MicroPostBean.class, resourcesToInsert);
	}
	
	@Test
	public void testUpsertAndRetrieveNewsArticle() throws Exception {
		testUpsertAndRetrieve(NewsArticleBean.class, getTestNewsArticles());
	}
	
	@Test
	public void testUpsertAndRetrieveTVProgram() throws Exception {
		testUpsertAndRetrieve(TVProgramBean.class, getTestTVPrograms());
	}
	
	@Test
	public void testUpsertAndRetrieveSubtitleSegment() throws Exception {
		testUpsertAndRetrieve(SubtitleSegment.class, getTestSubtitleSegments());
	}

	private <T extends XLiMeResource> void testUpsertAndRetrieve(Class<T> clazz, List<T> resourcesToInsert) {
		Properties props = new Properties();
		props.put(ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getKey(), "test-xlimeress");
		MongoXLiMeResourceStorer dao = new MongoXLiMeResourceStorer(props);
		
		//test insert
		if (resourcesToInsert.isEmpty()) throw new RuntimeException("test must provide some resources");
		for (T resource: resourcesToInsert) {
			String id = dao.insertOrUpdate(resource);
			assertNotNull(id);
		}
		
		//test find
		Optional<? extends XLiMeResource> optIt = dao.findResource(clazz, resourcesToInsert.get(0).getUrl());
		assertTrue(optIt.isPresent());
		System.out.println("Retrieved resource " + optIt.get());
		assertEquals(resourcesToInsert.get(0).getUrl(), optIt.get().getUrl());
	}
	
	private List<TVProgramBean> getTestTVPrograms() {
		return MediaItemDaoFromDatasetTest.findTestTVPrograms();
	}

	
	private List<NewsArticleBean> getTestNewsArticles() {
		return MediaItemDaoFromDatasetTest.findTestNewsArticles();
	}
	

	private List<MicroPostBean> getTestMicroPosts() {
		return MediaItemDaoFromDatasetTest.findTestMicroPosts();
	}

	private List<SubtitleSegment> getTestSubtitleSegments() {
		return MediaItemAnnotationDaoFromDatasetTest.findTestSubtitleSegments();
	}
	
}
