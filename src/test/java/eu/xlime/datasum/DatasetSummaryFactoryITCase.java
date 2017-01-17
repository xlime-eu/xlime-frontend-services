package eu.xlime.datasum;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import eu.xlime.datasum.DatasetSummaryFactory;
import eu.xlime.datasum.DatasetSummaryFactoryImpl;
import eu.xlime.datasum.bean.DatasetSummary;
import eu.xlime.datasum.bean.TimelineChart;

public class DatasetSummaryFactoryITCase {

	@Test
	public void test_createXLiMeSparqlSummary() {
		DatasetSummaryFactory testObj = new DatasetSummaryFactoryImpl();
		DatasetSummary sum = testObj.createXLiMeSparqlSummary();
		assertNotNull(sum);
		System.out.println("Summary: " + sum);
		assertTrue(sum != null);
	}

	@Test
	public void test_createXLiMeMongoSummary() {
		DatasetSummaryFactory testObj = new DatasetSummaryFactoryImpl();
		DatasetSummary sum = testObj.createXLiMeMongoSummary();
		assertNotNull(sum);
		System.out.println("Summary: " + sum);
		assertTrue(sum != null);
	}
	
	@Test
	public void test_retrieveTimelineChart01() {
		DatasetSummaryFactory testObj = new DatasetSummaryFactoryImpl();
		String topic = "news";
		String metric = "messagesProcessed-last-count";
		List<TimelineChart> chart = testObj.retrieveTimelineCharts(topic, metric);
		assertNotNull(chart);
		assertEquals("" + chart, "", chart.get(0).getUrl());
	}
	
	@Test
	public void test_retrieveAvailableTimelineResourceTypes() {
		DatasetSummaryFactory testObj = new DatasetSummaryFactoryImpl();
		List<String> list= testObj.retrieveAvailableTimelineResourceTypes();
		assertNotNull(list);
		assertTrue(!list.isEmpty());
		System.out.println("Available timeline resource types :" + list);
		assertTrue(true);
	}

	@Test
	public void test_retrieveAvailableMetricsForResourceType() {
		DatasetSummaryFactoryImpl testObj = new DatasetSummaryFactoryImpl();
		List<String> list= testObj.retrieveAvailableMetricsForResourceType("news");
		assertNotNull(list);
		assertTrue(!list.isEmpty());
		System.out.println("Available timeline resource types :" + list);
		assertTrue(true);
	}
	
}
