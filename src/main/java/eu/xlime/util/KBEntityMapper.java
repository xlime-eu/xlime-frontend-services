package eu.xlime.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import com.google.common.collect.ImmutableSet;

import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.sparql.SparqlQueryFactory;

/**
 * Provides services for mapping entity URIs from different KBs or different languages to
 * some <i>canonical entity</i>, usually by resolving <code>owl:sameAs</code> chains.
 *  
 * @author RDENAUX
 *
 */
public class KBEntityMapper {
	
	private static final Logger log = LoggerFactory.getLogger(KBEntityMapper.class);
	
	public static final SparqlQueryFactory qFactory = new SparqlQueryFactory();

	private static Cache<String, Set<?>> sameAsCache = CacheFactory.instance.buildCache("sameAsCache");
	
	private static final int maxRecentSameAsSize = 10;

	private static List<Set<String>> recentSameAsSets = new ArrayList<Set<String>>();
	
	private static Set<String> langWhitelist = ImmutableSet.of("en", "de", "es", "fr", "ru", "it");

	public Optional<String> toCanonicalEntityUrl(String entUrl) {
		if (isMainDBpediaEntity(entUrl)) {
			log.trace("Entity " + entUrl + " is already canonical");
			return Optional.of(entUrl);
		}
		if (isLangDependentDBpediaEntity(entUrl)) {
			log.trace("Entity " + entUrl + " is from language-dependent dbpedia");			
			return toMainDBpedia(entUrl);
		}
		if (isWikiEnt(entUrl)) {
			log.trace("Entity " + entUrl + " is from wikipedia");						
			return toMainDBpedia(rewriteWikiToDBpedia(entUrl));
		}
		log.trace("Entity is from unknown KB " + entUrl);
		return Optional.absent();
	}

	private Optional<String> toMainDBpedia(Optional<String> langDBpediaRes) {
		if (langDBpediaRes.isPresent()) return toMainDBpedia(langDBpediaRes.get());
		else return langDBpediaRes;
	}
	
	private Optional<String> toMainDBpedia(final String langDBpediaRes) {
		if (isMainDBpediaEntity(langDBpediaRes)) return Optional.of(langDBpediaRes);
				
		Callable<? extends Set<?>> valueLoader = new Callable<Set<?>>() {

			@Override
			public Set<?> call() throws Exception {
				String msg = "Retrieve sameAs " + langDBpediaRes + " from dbpedia"; 
				log.info(msg);
				SparqlClient om = getDBpediaSparqlClient();
				String query = qFactory.sameAs(langDBpediaRes);
				log.trace("query: " + query);
				Map<String, Map<String, String>> result = om.executeSPARQLQuery(query);

				return extractSet(result, "sameAs");
			}

			private Set<String> extractSet(
					Map<String, Map<String, String>> resultSet, String key) {
				if (resultSet == null || resultSet.keySet().isEmpty()) {
					log.debug("No results");
					return ImmutableSet.of();
				}
				Set<String> result = new HashSet<>();
				for (String id: resultSet.keySet()) {
					Map<String, String> tuple = resultSet.get(id);
					if (tuple.containsKey(key)) {
						result.add(tuple.get(key));
					}
				}				
				return result;
			}
		};
		
		try {
			log.trace("Retrieving main DBpedia resource for " + langDBpediaRes);
			Set<String> sameAsSet = getSameAsSet(langDBpediaRes, valueLoader);
			Optional<String> result = findMainDBpedia(sameAsSet);
			log.trace("Retrieved main DBpedia resource " + result);			
			return result; 
		} catch (ExecutionException e) {
			log.warn("Error loading sameAs values for " + langDBpediaRes, e);
			return Optional.absent();
		}
	}

	private Set<String> getSameAsSet(String langDBpediaRes,
			Callable<? extends Set<?>> valueLoader) throws ExecutionException {
		Optional<Set<String>> optResult = findRecentSameAsSet(langDBpediaRes);
		if (optResult.isPresent()) return optResult.get();
		
		@SuppressWarnings("unchecked")
		Set<String> result = (Set<String>) sameAsCache.get(langDBpediaRes, valueLoader);

		pushRecentSameAsSet(result, langDBpediaRes);
		
		return result;
	}

	private void pushRecentSameAsSet(Set<String> result, String langDBpediaRes) {
		recentSameAsSets.add(0, ImmutableSet.<String>builder().addAll(result).add(langDBpediaRes).build());
		if (recentSameAsSets.size() > maxRecentSameAsSize) {
			recentSameAsSets.remove(maxRecentSameAsSize);
		}
	}

	private Optional<Set<String>> findRecentSameAsSet(String langDBpediaRes) {
		for (Set<String> set: recentSameAsSets) {
			if (set.contains(langDBpediaRes)) return Optional.of(set);
		}
		return Optional.absent();
	}

	private boolean isMainDBpediaEntity(final String langDBpediaRes) {
		return langDBpediaRes.contains("http://dbpedia.org/");
	}

	private Optional<String> findMainDBpedia(Set<?> set) {
		for (Object url: set) {
			if (url instanceof String) {
				String surl = (String)url;
				if (isMainDBpediaEntity(surl)) return Optional.of(surl); 
			}
		}
		log.debug("Found no main dbpedia entity in " + set);
		return Optional.absent();
	}

	final boolean isWikiEnt(String entUrl) {
		return entUrl.matches("http://(\\w\\w).wikipedia.org/wiki/.*");
	}

	final boolean isLangDependentDBpediaEntity(String entUrl) {
		return entUrl.matches("http://(\\w\\w).dbpedia.org/resource/.*");
	}
	
	private Optional<String> rewriteWikiToDBpedia(String entUrl) {
		//TODO: better to use a regular expression to extract these values?
		String langDom = entUrl.substring(7, 9);
		if (shouldIgnoreLang(langDom)) {
			log.debug("Not rewriting wiki to dbpedia for lang " + langDom);
			return Optional.absent();
		}
		langDom = langDom + ".";
		if (langDom.equals("en.")) langDom = "";
		String entName = entUrl.substring(29);
		return Optional.of(String.format("http://%sdbpedia.org/resource/%s", langDom, entName));
	}
	
	private boolean shouldIgnoreLang(String lang) {
		return !langWhitelist.contains(lang);
	}

	private SparqlClient getDBpediaSparqlClient() {
//		return new SparqlClientFactory().getDBpediaSparqlClient();
		return new SparqlClientFactory().getXliMeSparqlClient(); // relevant sameAs triples already in xLiMe endpoint
	}

	final String decodedUrl(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	final boolean isEncodedUrl(String url) {
		return !decodedUrl(url).equals(url);
	}
	
}
