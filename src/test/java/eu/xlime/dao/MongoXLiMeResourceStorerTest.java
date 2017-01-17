package eu.xlime.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.DBSort;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.dao.annotation.MediaItemAnnotationDaoFromDatasetTest;
import eu.xlime.dao.mediaitem.MediaItemDaoFromDatasetTest;
import eu.xlime.datasum.bean.TimelineChart;
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
	
	@Test
	@Ignore("Requires TimelineChart to be available")
	public void getTimelineChart() throws Exception {
		Properties props = new Properties();
		props.put(ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getKey(), "xlimeres");
		MongoXLiMeResourceStorer dao = new MongoXLiMeResourceStorer(props);

//		assertEquals(3, dao.getDBCollection(TimelineChart.class).find().count());
		TimelineChart chart = dao.getDBCollection(TimelineChart.class).find().next();
		System.out.println("Chart: " + chart);
		assertNotNull(chart);
	}

	@Test
	public void textSearchWithDates() throws Exception {
		Properties props = new Properties();
		props.put(ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getKey(), "xlimeres");
		MongoXLiMeResourceStorer dao = new MongoXLiMeResourceStorer(props);
		
		String text = "Nike";
		
		DBObject textQ = new BasicDBObject(
			    "$text", new BasicDBObject("$search", text)
				);
		DBObject projection = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				);
		DBObject sorting = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				); 
		
		ISO8601DateFormat format = new ISO8601DateFormat();
		Date d = format.parse("2016-10-07T11:58:02Z");

		DBObject textAndDateQ = new BasicDBObject(
			    "$text", new BasicDBObject("$search", text)
				).append("created.timestamp", new BasicDBObject("$lte", d));
		DBObject lteDateQ = new BasicDBObject(
			    "created.timestamp", new BasicDBObject("$lte", d)
		); 

		DBCursor<MicroPostBean> cursorWithDateFilter = dao.getDBCollection(MicroPostBean.class).find(textAndDateQ, projection).sort(sorting).sort(DBSort.desc("created.timestamp"));
		DBCursor<MicroPostBean> cursorNoDateFilter = dao.getDBCollection(MicroPostBean.class).find(textQ, projection).sort(sorting).sort(DBSort.desc("created.timestamp"));		
		DBCursor<MicroPostBean> cursorNoTextFilter = dao.getDBCollection(MicroPostBean.class).find().lessThan("created.timestamp", d).sort(sorting).sort(DBSort.desc("created.timestamp"));
		DBCursor<MicroPostBean> cursorManualDateFilter = dao.getDBCollection(MicroPostBean.class).find(lteDateQ).sort(sorting).sort(DBSort.desc("created.timestamp"));
		System.out.println("withDateFilter: " + cursorWithDateFilter.count());
		System.out.println("noDateFilter: " + cursorNoDateFilter.count());
		System.out.println("notextFilter: " + cursorNoTextFilter.count());
		System.out.println("manualDateFilter: " + cursorManualDateFilter.count());		
		
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
