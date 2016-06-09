package eu.xlime.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.apache.jena.riot.Lang;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.query.Dataset;

import eu.xlime.testkit.DatasetLoader;

public class JenaSparqlClientImplTest {

	private static Logger log = LoggerFactory.getLogger(JenaSparqlClientImplTest.class);
	private DatasetLoader dsLoader = new DatasetLoader();
	
	@Test
	public void testQueryDataset() {
		String fpath = "src/test/resources/socmedia-example-graph.trig";
		Optional<Dataset> ds = dsLoader.loadDataset(new File(fpath), Lang.TRIG);
		assertTrue(ds.isPresent());
		JenaSparqlClientImpl testObj = new JenaSparqlClientImpl(ds.get());

		String q = queryMediaItemsUrls();
		System.out.println("query: \n" + q + "---end query---");
		Map<String, Map<String, String>> result = testObj.executeSPARQLQuery(q);
		assertNotNull(result);
		System.out.println("Media items in " + fpath + ": " + result);
		assertFalse(result.isEmpty());
		assertEquals(35, result.size());
	}

	@Test
	public void testQueryBrexitDataset() {
		String fpath = "src/test/resources/socmedia-example-graph.trig";
		Optional<Dataset> ds = dsLoader.loadDataset(new File(fpath), Lang.TRIG);
		assertTrue(ds.isPresent());
		JenaSparqlClientImpl testObj = new JenaSparqlClientImpl(ds.get());

		String q = queryBrexitSocMediaItemsUrls();
		System.out.println("query: \n" + q + "---end query---");
		Map<String, Map<String, String>> result = testObj.executeSPARQLQuery(q);
		assertNotNull(result);
		System.out.println("Media items in " + fpath + ": " + result);
		assertFalse(result.isEmpty());
		assertEquals(27, result.size());
	}
	
	private String queryBrexitSocMediaItemsUrls() {
		SparqlQueryFactory qFactory = new SparqlQueryFactory();
		return qFactory.microPostsByKeywordFilter(ImmutableList.of("Brexit EN", "Brexit ES", "Brexit DE", "Brexit IT"));
	}

	private String queryMediaItemsUrls() {
		SparqlQueryFactory qFactory = new SparqlQueryFactory();
		return qFactory.mediaItemUrls();
	}
	

}
