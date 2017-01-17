package eu.xlime.datasum;

import java.util.List;

import eu.xlime.datasum.bean.DatasetSummary;
import eu.xlime.datasum.bean.TimelineChart;

public interface DatasetSummaryFactory {
	
	DatasetSummary createXLiMeSparqlSummary();
	DatasetSummary createXLiMeMongoSummary();

	/**
	 * Retrieves (or generates) a TimelineChart from the underlying database.
	 * 
	 * @param resourceType
	 * @param metric
	 * @return
	 */
	List<TimelineChart> retrieveTimelineCharts(String resourceType, String metric); // throws UnavailableChartException;
	
	List<String> retrieveAvailableTimelineResourceTypes();
	List<String> retrieveAvailableMetricsForResourceType(String resourceTypeName);
	/*
	public static class UnavailableChartException extends RuntimeException {
		public UnavailableChartException(String message, String availables) {
			super(String.format("%s.\nAvailable charts %s", message, availables));
		}
	}*/
	
}