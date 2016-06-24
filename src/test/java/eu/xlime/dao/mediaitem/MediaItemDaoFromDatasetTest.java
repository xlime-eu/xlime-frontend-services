package eu.xlime.dao.mediaitem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.query.Dataset;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
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
