package eu.xlime.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.base.Optional;

import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.mongo.ConfigOptions;

public class MongoMediaItemDaoITCase {

	@Test
	public void testUpsertAndRetrieveMicroPost() {
		Properties props = new Properties();
		props.put(ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getKey(), "test-xlimeress");
		MongoMediaItemDao dao = new MongoMediaItemDao(props);
		
		//test insert
		List<MicroPostBean> microPosts = getTestMicroPosts();
		for (MicroPostBean microPost: microPosts) {
			String id = dao.insertOrUpdate(microPost);
			assertNotNull(id);
		}
		
		//test find
		Optional<? extends MediaItem> optIt = dao.findMediaItem(microPosts.get(0).getUrl());
		assertTrue(optIt.isPresent());
		System.out.println("Retrieved media item " + optIt.get());
		assertEquals(microPosts.get(0).getUrl(), optIt.get().getUrl());
	}

	@Test
	public void testFindMicroPostsByDate() throws ParseException {
		Properties props = new Properties();
		props.put(ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getKey(), "brexit-xlimeress");
		MongoMediaItemDao dao = new MongoMediaItemDao(props);
		
		long dateFrom = ISO8601Utils.parse("2016-06-01T07:00:00Z", new ParsePosition(0)).getTime();
		long dateTo =   ISO8601Utils.parse("2016-06-01T08:00:00Z", new ParsePosition(0)).getTime();
		List<MicroPostBean> mpbs = dao.findMicroPostsByDate(dateFrom, dateTo, 50);
		assertFalse(mpbs.isEmpty());
		System.out.println("Retrieved microposts " + mpbs.size());
		assertEquals(50, mpbs.size()); //microPosts.get(0).getUrl(), optIt.get().getUrl());
	}
	
	private List<MicroPostBean> getTestMicroPosts() {
		return MediaItemDaoFromDatasetTest.findTestMicroPosts();
	}
}
