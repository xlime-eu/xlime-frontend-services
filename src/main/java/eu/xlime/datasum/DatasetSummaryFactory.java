package eu.xlime.datasum;

import eu.xlime.datasum.bean.DatasetSummary;

public interface DatasetSummaryFactory {

	public abstract DatasetSummary createXLiMeSparqlSummary();

	public abstract DatasetSummary createXLiMeMongoSummary();

}