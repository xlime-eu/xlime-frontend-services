package eu.xlime.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.mydailyjava.guava.cache.overflow.FileSystemCacheBuilder;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;

import eu.xlime.bean.MediaItem;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.sparql.SparqlQueryFactory;

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

	private static Cache<String, List<String>> searchEntityCache = FileSystemCacheBuilder.newBuilder()
			.maximumSize(1L) // In-memory, rest goes to disk
			.persistenceDirectory(new File("target/searchEntityCache/"))
			.softValues()
			.build();
	
	private static Cache<String, List<String>> searchStringCache = FileSystemCacheBuilder.newBuilder()
			.maximumSize(1L) // In-memory, rest goes to disk
			.persistenceDirectory(new File("target/searchStringCache/"))
			.softValues()
			.build();
	
	private static Cache<String, List<String>> searchFreeTextCache = FileSystemCacheBuilder.newBuilder()
			.maximumSize(1L) // In-memory, rest goes to disk
			.persistenceDirectory(new File("target/searchFreeTextCache/"))
			.softValues()
			.build();

	public Optional<List<String>> findMediaItemUrlsByKBEntity(final String entity_url){
		Callable<? extends List<String>> valueLoader = new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				return retrieveMediaItemUrlsFromEntity(entity_url).get();
			}
		};

		try {
			return Optional.of(searchEntityCache.get(entity_url, valueLoader));
		} catch (ExecutionException e) {
			log.warn("Error loading searchEntity result for " + entity_url, e);
			return Optional.absent();
		}
	}
	
	public Optional<List<String>> findMediaItemUrlsByText(final String text){
		Callable<? extends List<String>> valueLoader = new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				return retrieveMediaItemUrlsFromText(text).get();
			}
		};

		try {
			return Optional.of(searchStringCache.get(text, valueLoader));
		} catch (ExecutionException e) {
			log.warn("Error loading searchString result for " + text, e);
			return Optional.absent();
		}
	}
	
	public Optional<List<String>> findMediaItemUrlsByFreeText(final String text){
		Callable<? extends List<String>> valueLoader = new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				return retrieveMediaItemUrlsFromFreeText(text).get();
			}
		};

		try {
			return Optional.of(searchFreeTextCache.get(text, valueLoader));
		} catch (ExecutionException e) {
			log.warn("Error loading searchFreeText result for " + text, e);
			return Optional.absent();
		}
	}
	
	/**
	 * Returns a list of media item urls by text input
	 * 
	 * @param text
	 * @return
	 */
	public Optional<List<String>>retrieveMediaItemUrlsFromText(String text) {
		AutocompleteClient client = new AutocompleteClient();
		Optional<AutocompleteBean> autocomplete = client.retrieveAutocomplete(text);
		String entity = autocomplete.get().getFirst_entity();
		return (findMediaItemUrlsByKBEntity(entity));
	}
	
	/**
	 * Returns a list of media item urls by free text input
	 * 
	 * @param text
	 * @return
	 */
	public Optional<List<String>>retrieveMediaItemUrlsFromFreeText(String text) {
		final SparqlClient sparqler = getXLiMeSparqlClient();
		List<String> urls = new ArrayList<String>();
		KITSearchClient client = new KITSearchClient();
		List<String> sources = client.retrieveKitsearch(text).get();
		for (String source : sources) {
			String query = qFactory.mediaItemUrisBySource(source);
			log.debug("Retrieving media items URI using: " + query);
			Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
			log.debug(String.format("Found %s media items for entity %s", result.size(), source));
			urls.addAll(toUrlList(result));
		}
		log.trace("Media Items: " + urls.toString());
		return Optional.of(urls);
	}
	
	/**
	 * Returns a list of media item urls by KB entity input
	 * 
	 * @param entity_url
	 * @return
	 */
	public Optional<List<String>> retrieveMediaItemUrlsFromEntity(String entity_url) {
		final SparqlClient sparqler = getXLiMeSparqlClient();
		//UIEntity entity = UIEntityFactory.instance.retrieveFromUri(entity_url);
		String query = qFactory.entityAnnotationInMediaItem(entity_url);
		log.debug("Retrieving media items URIs using: " + query);
		Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
		log.debug(String.format("Found %s media items for entity %s", result.size(), entity_url));
		//log.trace("Entity: " + entity.toString());
		List<String> urls = toUrlList(result);
		log.trace("Media Items: " + urls.toString());
		return Optional.of(urls);
	}
	
	private List<String> toUrlList(Map<String, Map<String, String>> resultSet) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("Empty resultset ");
			return ImmutableList.of();
		}
		List<String> result = new ArrayList<>();
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			result.add(tuple.get("s"));
		}
		return result;
	}
	
	private SparqlClient getXLiMeSparqlClient() {
		return new SparqlClientFactory().getXliMeSparqlClient();
	}
}
