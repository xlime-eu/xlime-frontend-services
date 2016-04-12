package eu.xlime.summa;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.mydailyjava.guava.cache.overflow.FileSystemCacheBuilder;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.isoco.kontology.access.OntologyManager;
import com.isoco.kontology.ontologies.dao.SesameDAOFactory;

import eu.xlime.Config;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.SparqlQueryFactory;

public class UIEntityFactory {

	public static final Logger log = LoggerFactory.getLogger(UIEntityFactory.class.getName());
	
	private OntologyManager dbpOntoManager;

	public static final SparqlQueryFactory qFactory = new SparqlQueryFactory();
	public static final UIEntityFactory instance = new UIEntityFactory();
	
	private static Cache<String, UIEntity> uiEntityCache = FileSystemCacheBuilder.newBuilder()
			.maximumSize(1L) // In-memory, rest goes to disk
			.persistenceDirectory(new File("target/uiEntityCache/"))
			.softValues()
			.build();
	
	
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

	private UIEntity doRetrieveFromUri(String entUri) {
		if (entUri.startsWith("http://dbpedia.org/")) {
			return retrieveUIEntityFromDBpedia(entUri).get();
		} else {
			throw new RuntimeException("Entity not supported " + entUri);
		}
	}

	private Optional<UIEntity> retrieveUIEntityFromDBpedia(final String entUri) {
		Callable<? extends UIEntity> valueLoader = new Callable<UIEntity>() {

			@Override
			public UIEntity call() throws Exception {
				String msg = "Retrieve " + entUri + " from dbpedia"; 
				log.info(msg);
				System.out.println(msg);
				OntologyManager om = getDBpediaOntoMan();
				String query = qFactory.dbpediaUIEntity(entUri, "en");
				log.debug("Retrieving entity info using: " + query);
				Map<String, Map<String, String>> result = om.executeAdHocSPARQLQuery(query);

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

	private OntologyManager getDBpediaOntoMan() {
		if (dbpOntoManager != null) return dbpOntoManager;
		Config cfg = new Config();
		dbpOntoManager =
				new SesameDAOFactory().createRemoteDAO(
						cfg.get(Config.Opt.DBpediaSparqlEndpoint),
						cfg.getDouble(Config.Opt.DBpediaSparqlRate));
		return dbpOntoManager;
	}
	
}
