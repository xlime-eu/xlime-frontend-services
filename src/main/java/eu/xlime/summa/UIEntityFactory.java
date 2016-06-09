package eu.xlime.summa;

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
import com.google.common.collect.ImmutableMap;

import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.sparql.SparqlQueryFactory;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.CacheFactory;

public class UIEntityFactory {

	public static final Logger log = LoggerFactory.getLogger(UIEntityFactory.class.getName());
	
	public static final SparqlQueryFactory qFactory = new SparqlQueryFactory();
	public static final UIEntityFactory instance = new UIEntityFactory();
	
	private static Cache<String, UIEntity> uiEntityCache = CacheFactory.instance.buildCache("uiEntityCache");
	
	private UIEntityFactory() {
	}
	
	/**
	 * Retrieves a {@link UIEntity} for a given entity URI.
	 * @param entUri
	 * @return
	 */
	public UIEntity retrieveFromUri(String entUri) {
		UIEntity result = doRetrieveFromUri(entUri);
		return cleanEntity(result);
	}
	
	public List<UIEntity> retrieveFromUris(List<String> uris) {
		Map<String, UIEntity> cached = uiEntityCache.getAllPresent(uris);
		List<String> toFind = new ArrayList(uris);
		toFind.removeAll(cached.keySet());
		
		Map<String, UIEntity> nonCached = doRetrieveFromUris(toFind);
		uiEntityCache.putAll(nonCached);
		
		log.debug(String.format("Found %s cached and %s uncached UIEntitys", cached.size(), nonCached.size()));
		
		return ImmutableList.<UIEntity>builder()
				.addAll(cleanEntities(ImmutableList.copyOf(cached.values())))
				.addAll(nonCached.values())
				.build();
	}

	/**
	 * <i>Cleans</i> an input entity and returns an entity which is better suitable to 
	 * be used in the front-end. For example, some types may be removed, or the list of 
	 * types may be sorted to put more common types at the beginning of the list.
	 *   
	 * @param entity
	 * @return
	 */
	private UIEntity cleanEntity(UIEntity entity) {
		Set<String> cleanTypes = new HashSet<>(entity.getTypes());
		for (String type: entity.getTypes()) {
			if (type.startsWith("http://dbpedia.org/class/yago/")) 
				cleanTypes.remove(type);
		}
		entity.setTypes(ImmutableList.copyOf(cleanTypes));
		return entity;
	}
	
	private List<UIEntity> cleanEntities(List<UIEntity> ents) {
		List<UIEntity> result = new ArrayList<>();
		for (UIEntity toClean: ents) {
			result.add(cleanEntity(toClean));
		}
		return ImmutableList.copyOf(result);
	}

	private UIEntity doRetrieveFromUri(String entUri) {
		if (entUri.startsWith("http://dbpedia.org/")) {
			return retrieveUIEntityFromDBpedia(entUri).get();
		} else {
			throw new RuntimeException("Entity not supported " + entUri);
		}
	}

	private Map<String, UIEntity> doRetrieveFromUris(List<String> entUris) {
		if (entUris.isEmpty()) return ImmutableMap.of();
		return retrieveUIEntitisFromDBpedia(entUris);
	}
	
	private Map<String, UIEntity> retrieveUIEntitisFromDBpedia(
			List<String> entUris) {
		// TODO Auto-generated method stub
		return null;
	}

	private Optional<UIEntity> retrieveUIEntityFromDBpedia(final String entUri) {
		Callable<? extends UIEntity> valueLoader = new Callable<UIEntity>() {

			@Override
			public UIEntity call() throws Exception {
				String msg = "Retrieve " + entUri + " from dbpedia"; 
				log.info(msg);
				SparqlClient sparqler = getDBpediaSparqlClient();
				String query = qFactory.dbpediaUIEntity(entUri, "en");
				if (log.isTraceEnabled()) {
					log.trace("Retrieving entity info using: " + query);
				}
				Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);

				return toUIEntity(result, entUri).get();
			}
		};
		
		try {
			return Optional.of(uiEntityCache.get(entUri, valueLoader));
		} catch (ExecutionException e) {
			log.warn("Error loading value for " + entUri, e);
			return Optional.absent();
		}
		
	}
	
	
	private Optional<UIEntity> toUIEntity(Map<String, Map<String, String>> resultSet, String entUri) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("No entity with " + entUri);
			return Optional.absent();
		}
		
		UIEntity result = new UIEntity();
		result.setUrl(entUri);
		Set<String> depictions = new HashSet<String>();
		Set<String> types = new HashSet<String>();		
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			if (tuple.containsKey("label")) {
				result.setLabel(tuple.get("label"));
			}
			if (tuple.containsKey("depiction")) {
				depictions.add(tuple.get("depiction"));
			}
			if (tuple.containsKey("type")) {
				types.add(tuple.get("type"));
			}
		}		
		result.setDepictions(ImmutableList.copyOf(depictions));
		result.setTypes(ImmutableList.copyOf(types));
		return Optional.of(result);
	}

	private SparqlClient getDBpediaSparqlClient() {
//		return new SparqlClientFactory().getDBpediaSparqlClient(); 
		return new SparqlClientFactory().getXliMeSparqlClient(); //relevant triples for entities already imported from DBpedia
	}
	
}
