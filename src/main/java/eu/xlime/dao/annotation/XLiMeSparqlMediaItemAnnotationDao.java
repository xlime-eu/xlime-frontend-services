package eu.xlime.dao.annotation;

import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.util.KBEntityMapper;
import eu.xlime.util.KBEntityMapperImpl;

public class XLiMeSparqlMediaItemAnnotationDao extends
		SparqlMediaItemAnnotationDao {

	private static KBEntityMapper kbEntMapper = new KBEntityMapperImpl();
	
	@Override
	protected SparqlClient getXliMeSparqlClient() {
		return new SparqlClientFactory().getXliMeSparqlClient();
	}

	@Override
	protected KBEntityMapper getKBEntityMapper() {
		return kbEntMapper;
	}

}
