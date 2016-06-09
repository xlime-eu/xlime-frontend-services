package eu.xlime.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import eu.xlime.Config;
import eu.xlime.Config.Opt;
import eu.xlime.bean.MediaItem;
import eu.xlime.bean.UrlLabel;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.sparql.SparqlQueryFactory;
import eu.xlime.util.CacheFactory;
import eu.xlime.util.KBEntityMapper;
import eu.xlime.util.score.ScoreFactory;
import eu.xlime.util.score.ScoredSet;
import eu.xlime.util.score.ScoredSetImpl;

/**
 * Provides methods for retrieving a search of {@link MediaItem} beans through text and KB entities.
 * 
 * @author Nuria Garcia
 * @email ngarcia@expertsystem.com
 *
 */

public class SearchItemDao {

	public static final Logger log = LoggerFactory.getLogger(SearchItemDao.class.getName());

	public static final SparqlQueryFactory qFactory = new SparqlQueryFactory();
	private static final KBEntityMapper kbEntityMapper = new KBEntityMapper();
	private static final ScoreFactory scoref = ScoreFactory.instance;

	private static Cache<String, ScoredSet<String>> searchEntityCache = CacheFactory.instance.buildCache("searchEntityCache");
	
	private static Cache<String, ScoredSet<String>> searchStringCache = CacheFactory.instance.buildCache("searchStringCache");

	/**
	 * Returns a list of xLiMe media-item urls that match a knowledge-base entity 
	 * (identified by its <code>entity-url</code>).
	 * 
	 * Currently, this match is based on annotations found in the media-items. 
	 *   
	 * @param entity_url
	 * @return
	 */
	public ScoredSet<String> findMediaItemUrlsByKBEntity(final String entity_url){
		Callable<? extends ScoredSet<String>> valueLoader = new Callable<ScoredSet<String>>() {
			@Override
			public ScoredSet<String> call() throws Exception {
				return retrieveMediaItemUrlsFromEntity(entity_url);
			}
		};

		try {
			return searchEntityCache.get(entity_url, valueLoader);
		} catch (ExecutionException e) {
			log.warn("Error loading searchEntity result for " + entity_url, e);
			return ScoredSetImpl.empty();
		}
	}
	
	/**
	 * Returns a list of xLiMe media-item urls that match an input text.
	 * 
	 * The matches may come from either resolving the text to some entity that has been 
	 * identified in the media-items or by a keyword search on the textual contents
	 * of the media-items.
	 * 
	 * @param text
	 * @return
	 */
	public ScoredSet<String> findMediaItemUrlsByText(final String text){
		Callable<? extends ScoredSet<String>> valueLoader = new Callable<ScoredSet<String>>() {
			@Override
			public ScoredSet<String> call() throws Exception {
				return retrieveMediaItemUrlsByText(text);
			}
		};

		try {
			return searchStringCache.get(text, valueLoader);
		} catch (ExecutionException e) {
			log.warn("Error loading searchString result for " + text, e);
			return ScoredSetImpl.empty();
		}
	}
	
	ScoredSet<String> retrieveMediaItemUrlsByText(String text) {
		ScoredSet<String> viaEntity = retrieveMediaItemUrlsFromTextViaEntities(text);
		ScoredSet<String> viaStringMatch = retrieveMediaItemUrlsFromFreeText(text);
		return mergeMediaItemResults(viaEntity, viaStringMatch);
	}
	
	private ScoredSet<String> mergeMediaItemResults(ScoredSet<String>... results) {
		ScoredSet.Builder<String> builder = ScoredSetImpl.builder();
		for (ScoredSet<String> result: results) {
			builder.addAll(result);
		}
		return builder.build();
	}

	
	/**
	 * Returns a list of media item urls for some text. The match is performed by trying 
	 * to resolve the input <code>text</code> to potential <code>KB entities</code> and then
	 * searching for media items that mention those entities (as annotations).
	 * 
	 * @see #retrieveMediaItemUrlsFromFreeText(String)
	 * 
	 * @param text
	 * @return
	 */
	ScoredSet<String> retrieveMediaItemUrlsFromTextViaEntities(String text) {
		List<UrlLabel> entities = autoCompleteEntities(text);
		
		if (entities.isEmpty()) return ScoredSetImpl.empty();
		String entity = entities.iterator().next().getUrl();
		return addJustif(findMediaItemUrlsByKBEntity(entity), 
				String.format("matches most likely entity for '%s'", text));
	}

	private ScoredSet<String> addJustif(
			ScoredSet<String> source, String justif) {
		ScoredSet.Builder<String> builder = ScoredSetImpl.builder();
		for (String val: source.unscored()) {
			builder.add(val, scoref.newScore(source.getScore(val).getValue(), justif));
		}
		return builder.build();
	}

	public List<UrlLabel> autoCompleteEntities(String text) {
		AutocompleteClient client = new AutocompleteClient();
		Optional<AutocompleteBean> autocomplete = client.retrieveAutocomplete(text);
		if (!autocomplete.isPresent()) return ImmutableList.of();
		List<UrlLabel> entities = autocomplete.get().getEntities();
		return entities;
	}
	
	/**
	 * Returns a list of media item urls by source lookup.
	 * 
	 * @param text
	 * @return
	 */
	ScoredSet<String> retrieveMediaItemUrlsFromFreeText(String text) {
		final SparqlClient sparqler = getXLiMeSparqlClient();
		ScoredSet.Builder<String> builder = ScoredSetImpl.builder();
		KITSearchClient client = new KITSearchClient();
		ScoredSet<String> sources = client.retrieveKitsearch(text);
		log.debug("Found " + sources.size() + " media-item sources for '" + text + "'");
		
		if (!sources.isEmpty()) {
			String query = qFactory.mediaItemUrisBySource(sources.asList());
			log.debug("Retrieving media items URI using: " + query);
			Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
			log.debug(String.format("Found %s media items for sources %s", result.size(), sources));
			builder.addAll(toUrlScoredSet(result));
		}
		
/*		for (String source : sources) {
			String query = qFactory.mediaItemUrisBySource(ImmutableList.of(source));
			log.debug("Retrieving media items URI using: " + query);
			Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
			log.debug(String.format("Found %s media items for source %s", result.size(), source));
			urls.addAll(toUrlList(result));
		}
		*/
		log.trace("Media Items: " + builder.toString());
		return builder.build();
	}
	
	/**
	 * Returns a list of media item urls for a KB entity input
	 * 
	 * @param entity_url
	 * @return
	 */
	ScoredSet<String> retrieveMediaItemUrlsFromEntity(String entity_url) {
		/* TODO: we may need to
		 *  1. add a timer to avoid having to wait for long queries
		 *  2. have multiple queries to ensure different media-item types (as order of standard query is uncertain, so only the same media types may be returned)
		 *  3. try multiple confidence values (if default 0.98 does not return values)
		 *  4. provide an option for doing cross-lingual or mono-lingual search?  
		 */
		final SparqlClient sparqler = getXLiMeSparqlClient();
		Config cfg = new Config();
//		Set<String> expandedEntities = ImmutableSet.of(entity_url); 
		Set<String> expandedEntities = kbEntityMapper.expandSameAs(entity_url);
		String query = qFactory.entityAnnotationInMediaItem(filterExpandedEntities(expandedEntities), 0.98);
		log.debug("Retrieving media items URIs using: " + query);
		Map<String, Map<String, String>> result = sparqler.executeSPARQLOrEmpty(query, cfg.getLong(Opt.SparqlTimeout));
		log.debug(String.format("Found %s media items for entity %s", result.size(), entity_url));
		
		ScoredSet<String> urls = toUrlScoredSet(result);
		log.trace("Media Items: " + urls.toString());
		return urls;
	}
	
	private Set<String> filterExpandedEntities(Set<String> inEntUrls) {
		Set<String> result = new HashSet<String>();
		Set<String> supported = getSupportedEntUriFilters(); 
		for (String url: inEntUrls) {
			for (String supFilter : supported) {
				if (url.contains(supFilter)) result.add(url);
			}
		}
		
		return result;
	}

	private Set<String> getSupportedEntUriFilters() {
		//TODO: make this configurable
		Set<String> supportedLangs = ImmutableSet.of("", "nl", "fr", "de", "it", "es");
		Set<String> supportedDomains = ImmutableSet.of("dbpedia.org", "wikipedia.org");
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		for (String lang: supportedLangs) {
			for (String dom: supportedDomains) {
				if ("".equals(lang)) builder.add("http://" + dom);
				else builder.add("http://" + lang + "." + dom);
			}
		}
		Set<String> supported = builder.build();
		return supported;
	}

	private ScoredSet<String> toUrlScoredSet(Map<String, Map<String, String>> resultSet) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("Empty resultset ");
			return ScoredSetImpl.empty();
		}
		ScoredSet.Builder<String> builder = ScoredSetImpl.builder();
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			builder.add(tuple.get("s"), scoref.newScore(readDouble(tuple, "c", 1.0)));
		}
		return builder.build();
	}
	
	private double readDouble(Map<String, String> tuple, String key, double d) {
		if (tuple.containsKey(key)) {
			String value = tuple.get(key);
			try {
				return Double.parseDouble(value);
			} catch (Exception e) {
				log.warn("Error parsing " + value, e.getLocalizedMessage());
				return d;
			}
		} else return d;
	}

	private SparqlClient getXLiMeSparqlClient() {
		return new SparqlClientFactory().getXliMeSparqlClient();
	}
}
