package eu.xlime.dao.mediaitem;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;

public class XLiMeSparqlMediaItemDao extends SparqlMediaItemDao {

	
	@Override
	protected SparqlClient getXLiMeSparqlClient() {
		return new SparqlClientFactory().getXliMeSparqlClient();
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
		// TODO implement
		return Optional.absent();
	}


}
