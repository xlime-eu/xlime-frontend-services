package eu.xlime.dao.mediaitem;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hp.hpl.jena.query.Dataset;

import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;

/**
 * Provides {@link MediaItem}s from a Jena {@link Dataset}.
 * 
 * The {@link Dataset} is used as a Sparql endpoint.
 *  
 * @author rdenaux
 *
 */
public class MediaItemDaoFromDataset extends SparqlMediaItemDao{

	private static final Logger log = LoggerFactory.getLogger(MediaItemDaoFromDataset.class);

	private final Dataset dataset;
	
	public MediaItemDaoFromDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	@Override
	protected SparqlClient getXLiMeSparqlClient() {
		return new SparqlClientFactory().fromDataset(dataset);
	}
	
	@Override
	public List<NewsArticleBean> findNewsArticles(List<String> uris) {
		return ImmutableList.copyOf(doFindNewsArticles(uris).values());
	}

	@Override
	public List<MicroPostBean> findMicroPosts(List<String> uris) {
		return ImmutableList.copyOf(doFindMicroPosts(uris).values());
	}

	@Override
	public List<TVProgramBean> findTVPrograms(List<String> uris) {
		return ImmutableList.copyOf(doFindTVPrograms(uris).values());
	}
	
	@Override
	public Optional<VideoSegment> findVideoSegment(String uri) {
		return Optional.absent();
	}


}
