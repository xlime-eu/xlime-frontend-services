package eu.xlime.dao.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;

import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.util.KBEntityMapper;

public class MediaItemAnnotationDaoFromDataset extends
		SparqlMediaItemAnnotationDao {

	private static final Logger log = LoggerFactory.getLogger(MediaItemAnnotationDaoFromDataset.class);

	private final Dataset dataset;
	private final KBEntityMapper kbEntityMapper;
	
	public MediaItemAnnotationDaoFromDataset(Dataset dataset, KBEntityMapper kbEntMapper) {
		this.dataset = dataset;
		this.kbEntityMapper = kbEntMapper;
	}
	
	@Override
	protected SparqlClient getXliMeSparqlClient() {
		return new SparqlClientFactory().fromDataset(dataset);
	}

	@Override
	protected KBEntityMapper getKBEntityMapper() {
		return kbEntityMapper;
	}

	
}
