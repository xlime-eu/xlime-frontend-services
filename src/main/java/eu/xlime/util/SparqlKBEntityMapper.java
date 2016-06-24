package eu.xlime.util;

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

public class SparqlKBEntityMapper extends BaseEnDBpedKBEntityMapper implements KBEntityMapper {

	private static final Logger log = LoggerFactory.getLogger(SparqlKBEntityMapper.class);
	
	public static final SparqlQueryFactory qFactory = new SparqlQueryFactory();

	private static Cache<String, Set<?>> sameAsCache = CacheFactory.instance.buildCache("sameAsCache");
	
	private static final int maxRecentSameAsSize = 10;

	private static List<Set<String>> recentSameAsSets = new ArrayList<Set<String>>();
	

	/*
	 * (non-Javadoc)
	 * @see eu.xlime.util.BaseKBEntityMapper#getDBpediaSameAsSet(java.lang.String)
	 */
	@Override
	protected Set<String> getDBpediaSameAsSet(final String langDBpediaRes) throws ExecutionException {
		Callable<? extends Set<?>> valueLoader = new Callable<Set<?>>() {

			@Override
			public Set<?> call() throws Exception {
				String msg = "Retrieve sameAs " + langDBpediaRes + " from dbpedia"; 
				log.info(msg);
				SparqlClient sparqler = getDBpediaSparqlClient();
				String query = qFactory.sameAs(langDBpediaRes);
				log.trace("query: " + query);
				Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);

				return ImmutableSet.builder().addAll(extractSet(result, "sameAs")).add(langDBpediaRes).build();
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
		
		log.trace("Retrieving main DBpedia resource for " + langDBpediaRes);
		return getSameAsSet(langDBpediaRes, valueLoader);
	}
	
	private Set<String> getSameAsSet(String langDBpediaRes,
			Callable<? extends Set<?>> valueLoader) throws ExecutionException {
		Optional<Set<String>> optResult = findRecentSameAsSet(langDBpediaRes);
		if (optResult.isPresent()) return optResult.get();
		
		@SuppressWarnings("unchecked")
		Set<String> result = (Set<String>) sameAsCache.get(langDBpediaRes, valueLoader);

		pushRecentSameAsSet(result);
		
		return result;
	}

	private void pushRecentSameAsSet(Set<String> sameAsSet) {
		recentSameAsSets.add(0, sameAsSet);
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

	private SparqlClient getDBpediaSparqlClient() {
//		return new SparqlClientFactory().getDBpediaSparqlClient();
		return new SparqlClientFactory().getXliMeSparqlClient(); // relevant sameAs triples already in xLiMe endpoint
	}

}
