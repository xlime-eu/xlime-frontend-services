package eu.xlime.dao.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlQueryFactory;
import eu.xlime.summa.bean.UIEntity;

/**
 * Retrieves {@link UIEntity}s 
 * @author rdenaux
 *
 */
public abstract class BaseSparqlUIEntityDao extends AbstractUIEntityDao {

	public static final Logger log = LoggerFactory.getLogger(BaseSparqlUIEntityDao.class.getName());
	public static final SparqlQueryFactory qFactory = new SparqlQueryFactory();

	/**
	 * Returns the {@link SparqlClient} that will be used to retrieve the {@link UIEntity} data. 
	 * It must follow the vocabulary used by DBpedia. 
	 * @return
	 */
	protected  abstract SparqlClient getDBpediaSparqlClient();
	
	@Override
	public Optional<UIEntity> retrieveFromUri(String entUri, Optional<Locale> optLocale) {
		try {
			UIEntity result = doRetrieveFromUri(entUri, optLocale);
			return Optional.of(cleanEntity(result));
		} catch (Exception e) {
			return Optional.absent();
		}
	}

	@Override
	public List<UIEntity> retrieveFromUris(List<String> uris, Optional<Locale> optLocale) {
		return cleanEntities(retrieveUIEntitiesFromDBpedia(uris, optLocale));
	}

	private UIEntity doRetrieveFromUri(String entUri, Optional<Locale> optLocale) {
		if (entUri.startsWith("http://dbpedia.org/")) {
			return retrieveUIEntitiesFromDBpedia(ImmutableList.of(entUri), optLocale).get(0);
		} else {
			throw new RuntimeException("Entity not supported " + entUri + " only use canonical entities. See KBEntityMapper.");
		}
	}

	private List<UIEntity> retrieveUIEntitiesFromDBpedia(final List<String> entUris, Optional<Locale> optLocale) {
		if (entUris.isEmpty()) return ImmutableList.of(); 
		String msg = "Retrieve " + entUris + " from dbpedia"; 
		log.info(msg);
		final long start = System.currentTimeMillis();
		SparqlClient sparqler = getDBpediaSparqlClient();
		String query = qFactory.dbpediaUIEntity(entUris, optLocale.or(Optional.of(Locale.UK)).get().getLanguage());
		if (log.isTraceEnabled()) {
			log.trace("Retrieving entity info using: " + query);
		}
		Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
		if (log.isDebugEnabled()) {
			log.debug(String.format("Executed sparql in %sms", (System.currentTimeMillis() - start)));
		}
		return ImmutableList.copyOf(toUIEntity(result, entUris).values());
	}

	private Map<String, UIEntity> toUIEntity(Map<String, Map<String, String>> resultSet, List<String> entUris) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("No entities for " + entUris);
			return ImmutableMap.of();
		}
		
		log.debug(String.format("Found %s results", resultSet.keySet().size()));
		
		SetMultimap<String, String> labels = HashMultimap.create();
		SetMultimap<String, String> depictions = HashMultimap.create();
		SetMultimap<String, String> types = HashMultimap.create();
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			if (!tuple.containsKey("uri")) {
				log.debug("Result tuple does not contain uri" + tuple);
				continue;
			}
			String uri = tuple.get("uri");
			
			if (tuple.containsKey("label")) {
				labels.put(uri, tuple.get("label"));
			}
			if (tuple.containsKey("depiction")) {
				depictions.put(uri, tuple.get("depiction"));
			}
			if (tuple.containsKey("type")) {
				types.put(uri, tuple.get("type"));
			}
		}
		Set<String> uris = ImmutableSet.<String>builder()
				.addAll(labels.keySet())
				.addAll(depictions.keySet())
				.addAll(types.keySet()).build();
		Map<String, UIEntity> result = new HashMap<>();
		for (String uri: uris) {
			UIEntity ent = new UIEntity();
			ent.setUrl(uri);
			if (labels.containsKey(uri)) {
				if (labels.get(uri).size() > 1) {
					log.debug(String.format("Entity %s has multiple labels %s, selecting one at random.", uri, labels.get(uri)));
				}
				ent.setLabel(labels.get(uri).iterator().next());
			}
			if (depictions.containsKey(uri)) {
				ent.setDepictions(ImmutableList.copyOf(depictions.get(uri)));
			}
			if (types.containsKey(uri)) {
				ent.setTypes(ImmutableList.copyOf(types.get(uri)));
			}
			result.put(uri, ent);
		}
		
		if (log.isDebugEnabled()) {
			log.debug(String.format("Found %s UIEntities (out of %s requested entities)", result.size(), entUris.size()));
		}
		return ImmutableMap.copyOf(result);
	}
	
}
