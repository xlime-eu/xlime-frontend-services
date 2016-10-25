package eu.xlime.dao.mediaitem;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.jena.riot.Lang;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hp.hpl.jena.query.Dataset;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.ZattooCustomTVInfo;
import eu.xlime.dao.mediaitem.MediaItemDaoFromDataset;
import eu.xlime.testkit.DatasetLoader;

public class MediaItemDaoFromDatasetTest {

	public static final List<String> brexitAllowedKeywords = ImmutableList.of("Brexit EN", "Brexit ES", "Brexit DE", "Brexit IT");
	
	private static DatasetLoader dsLoader = new DatasetLoader();

	@Test @Ignore("Eventually this test will fail as we are expecting 'recent' media items... check if we can add")
	public void testFindLatestMediaItemUrls() throws Exception {
		MediaItemDaoFromDataset testObj = createTestMediaItemDaoFromDataset();
		//TODO use findMediaItemsByDate with some known date range 
		List<String> urls = testObj.findLatestMediaItemUrls(1000, 100);
		assertNotNull(urls);
		System.out.println("Found 'latest' media items: " + urls);
		assertFalse(urls.isEmpty());
	}

	private MediaItemDaoFromDataset createTestMediaItemDaoFromDataset(String fpath) {
		Optional<Dataset> ds = dsLoader.loadDataset(new File(fpath), Lang.TRIG);
		assertTrue(ds.isPresent());

		MediaItemDaoFromDataset testObj = new MediaItemDaoFromDataset(ds.get());
		return testObj;
	}
	
	private MediaItemDaoFromDataset createTestMediaItemDaoFromDataset() {
		return createTestMediaItemDaoFromDataset("src/test/resources/socmedia-example-graph.trig");
	}

	/**
	 * Provides a list of test {@link MicroPostBean}s. Useful for other
	 * tests.
	 *  
	 * @return
	 */
	static public List<MicroPostBean> findTestMicroPosts() {
		MediaItemDaoFromDatasetTest inst = new MediaItemDaoFromDatasetTest();
		MediaItemDaoFromDataset testObj = inst.createTestMediaItemDaoFromDataset();
		List<String> urls = testObj.findMicroPostUrlsByKeyword(brexitAllowedKeywords);
		return testObj.findMicroPosts(urls);
	}

	static public List<NewsArticleBean> findTestNewsArticles() {
		MediaItemDaoFromDatasetTest inst = new MediaItemDaoFromDatasetTest();
		MediaItemDaoFromDataset testObj = inst.createTestMediaItemDaoFromDataset("src/test/resources/jsi-newsfeed-example-graph.trig");
		List<String> urls = testObj.findAllMediaItemUrls(100);
		return testObj.findNewsArticles(urls);
	}
	
	static public List<TVProgramBean> findTestTVPrograms() {
		MediaItemDaoFromDatasetTest inst = new MediaItemDaoFromDatasetTest();
		MediaItemDaoFromDataset testObj = inst.createTestMediaItemDaoFromDataset("src/test/resources/zattoo-epg-example-graph.trig");
		List<String> urls = testObj.findAllMediaItemUrls(100);
		return testObj.findTVPrograms(urls);
	}
	
	@Test
	public void testLoadTVProgram01() {
		MediaItemDaoFromDatasetTest inst = new MediaItemDaoFromDatasetTest();
		MediaItemDaoFromDataset testObj = inst.createTestMediaItemDaoFromDataset("src/test/resources/zattoo-epg-example-graph.trig");
		List<String> urls = testObj.findAllMediaItemUrls(100);
		List<TVProgramBean> beans = testObj.findTVPrograms(urls);
		assertEquals(1, beans.size());
		TVProgramBean tvb = beans.get(0);
		System.out.println("found " + tvb);
		assertEquals("http://zattoo.com/program/113962660", tvb.getUrl());
		final double delta = 0.05;
		assertEquals(360.0, tvb.getDuration().getTotalSeconds(), delta);
		assertEquals("France 24 [en]", tvb.getPublisher().getLabel());
		assertNull(tvb.getPublisher().getUrl());
		assertNull(tvb.getRelatedImage());
		assertEquals("SPORTS", tvb.getTitle());
		//assertEquals("http://zattoo-production-zapi-sandbox.zattoo.com/watch/france-24-en/113962660/1465563060000/1465563420000", tvb.getWatchUrl());		
	}

	@Test
	public void testLoadTVProgram02() {
		MediaItemDaoFromDatasetTest inst = new MediaItemDaoFromDatasetTest();
		MediaItemDaoFromDataset testObj = inst.createTestMediaItemDaoFromDataset("src/test/resources/zattoo-epg-example-graph2.trig");
		List<String> urls = testObj.findAllMediaItemUrls(100);
		List<TVProgramBean> beans = testObj.findTVPrograms(urls);
		assertEquals(1, beans.size());
		TVProgramBean tvb = beans.get(0);
		System.out.println("found " + tvb);
		assertEquals("http://zattoo.com/program/117082419", tvb.getUrl());
		final double delta = 0.05;
		assertEquals(120.0, tvb.getDuration().getTotalSeconds(), delta);
		assertEquals("France 24 [fr]", tvb.getPublisher().getLabel());
		assertNull(tvb.getPublisher().getUrl());
		assertNull(tvb.getRelatedImage());
		assertEquals("Météo", tvb.getTitle());
		//assertEquals("http://zattoo-production-zapi-sandbox.zattoo.com/watch/france-24-fr/117082419/1475740680000/1475740800000", tvb.getWatchUrl());		
	}

	@Test
	public void testLoadTVProgram03() {
		MediaItemDaoFromDatasetTest inst = new MediaItemDaoFromDatasetTest();
		MediaItemDaoFromDataset testObj = inst.createTestMediaItemDaoFromDataset("src/test/resources/zattoo-epg-example-graph3.trig");
		List<String> urls = testObj.findAllMediaItemUrls(100);
		List<TVProgramBean> beans = testObj.findTVPrograms(urls);
		assertEquals(1, beans.size());
		TVProgramBean tvb = beans.get(0);
		System.out.println("found " + tvb);
		String expUrl = "http://zattoo.com/program/117497971";
		assertEquals(expUrl, tvb.getUrl());
		final double delta = 0.05;
		assertEquals(2700.0, tvb.getDuration().getTotalSeconds(), delta);
		assertEquals("RSI La1 HD", tvb.getPublisher().getLabel());
		assertNull(tvb.getPublisher().getUrl());
		assertNull(tvb.getRelatedImage());
		assertEquals("Psych", tvb.getTitle());
		assertEquals("Series", tvb.getGenre());
		assertEquals("http://zattoo-production-zapi-sandbox.zattoo.com/watch/rsi-la1/117497971/1477038900000/1477041600000", tvb.getWatchUrl());
		assertNotNull(tvb.getCustomInfo());
		assertTrue(tvb.getCustomInfo() instanceof ZattooCustomTVInfo);
		ZattooCustomTVInfo zinfo = (ZattooCustomTVInfo)tvb.getCustomInfo();
		assertEquals("rsi-la1", zinfo.getChannelId());
		assertEquals(118148822L, zinfo.getProductionProgId());
		
		
		Map<String, Long> pidMappings = testObj.findZattooProductionPidMappings();
		assertFalse(pidMappings.isEmpty());
		assertEquals(ImmutableSet.of(expUrl), pidMappings.keySet());
		assertEquals(118148822L, (long)pidMappings.get(expUrl));
	}

	@Test
	public void testLoadTVProgram04() {
		MediaItemDaoFromDatasetTest inst = new MediaItemDaoFromDatasetTest();
		MediaItemDaoFromDataset testObj = inst.createTestMediaItemDaoFromDataset("src/test/resources/zattoo-epg-example-graph4.trig");
		List<String> urls = testObj.findAllMediaItemUrls(100);
		List<TVProgramBean> beans = testObj.findTVPrograms(urls);
		Map<String, Long> pidMappings = testObj.findZattooProductionPidMappings();
		assertFalse(pidMappings.isEmpty());
		String expUrl = "http://zattoo.com/program/117606865";
		assertEquals(ImmutableSet.of(expUrl), pidMappings.keySet());
		assertEquals(118324680L, (long)pidMappings.get(expUrl));
		assertEquals(0, beans.size());
		
	}
	
	@Test
	public void testFindMicroPostsByKeyword() throws Exception {
		MediaItemDaoFromDataset testObj = createTestMediaItemDaoFromDataset();
		List<String> urls = testObj.findMicroPostUrlsByKeyword(brexitAllowedKeywords);
		assertNotNull(urls);
		System.out.println("Found 'brexit' media items: " + urls);
		assertFalse(urls.isEmpty());
		
		List<MicroPostBean> beans = testObj.findMicroPosts(urls);
		assertNotNull(beans);
		System.out.println("Retrieved " + beans.size() + " MicroPostBeans");
		assertFalse(beans.isEmpty());
	}

}
