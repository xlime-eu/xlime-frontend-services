package eu.xlime.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openrdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;

import eu.xlime.Config;
import eu.xlime.util.CacheFactory;
import eu.xlime.util.score.ScoreFactory;
import eu.xlime.util.score.ScoredSet;
import eu.xlime.util.score.ScoredSetImpl;

/**
 * Java client implementation for the <a href="http://km.aifb.kit.edu/services/xlimesearch/kitsearch">KIT Search Service</a>.
 * 
 * @author Nuria Garcia 
 * @email ngarcia@expertsystem.com
 *
 */

public class KITSearchClient {

	private static Logger log = LoggerFactory.getLogger(KITSearchClient.class);

	private static MediaType textJson = new MediaType("text", "json");
	private static final ScoreFactory scoref = ScoreFactory.instance;

	/**
	 * Cache for recently retrieved kitsearch{@link Model}. Avoids having to call 
	 * the kitsearch server too often when requesting known keywords.
	 */
	private static Cache<String, ScoredSet<String>> kitsearchModelCache = CacheFactory.instance.buildCache("kitSearchModelCache");

	public ScoredSet<String> retrieveKitsearchModel(final String keywords) {
		Callable<? extends ScoredSet<String>> valueLoader = new Callable<ScoredSet<String>>() {
			@Override
			public ScoredSet<String> call() throws Exception {
				return retrieveKitsearchModelFromServer(keywords);
			}
		};

		try {
			return kitsearchModelCache.get(keywords, valueLoader);
		} catch (ExecutionException e) {
			log.warn("Error loading  kitsearchModel for " + keywords, e);
			return ScoredSetImpl.empty();
		}
	}

	public  ScoredSet<String> retrieveKitsearchModelFromServer(String keywords){
		log.debug("Retrieving KITSearch Model for " + keywords);
		Config cfg = new Config();
		Client client = ClientBuilder.newClient();
		WebTarget kitsearch = client.target(cfg.get(Config.Opt.XLiMeSearch)); 
		WebTarget qsearch = kitsearch.path("kitsearch");

		WebTarget target = qsearch.queryParam("q", keywords);

		Invocation.Builder invocationBuilder = target.request(textJson);

		Response resp = invocationBuilder.get();
		int status = resp.getStatus();
		log.debug("Response status: " + status);
		if (status != 200) {
			log.debug("Error retrieving KITSearch " + resp.getStatusInfo());
			return ScoredSetImpl.empty();
		}
		log.trace("response has entity: " + resp.hasEntity());
		if (resp.hasEntity()) {
			final String json = resp.readEntity(String.class);
			log.trace("Resp entity: " + json);
			return toAutocomplete(json);
		} else {
			return ScoredSetImpl.empty();
		}		
	}

	public ScoredSet<String> retrieveKitsearch(String keywords) {
		return retrieveKitsearchModel(keywords);		
	}

	private ScoredSet<String> toAutocomplete(String json) {
		ScoredSet.Builder<String> builder = ScoredSetImpl.builder();
		try {
			log.trace("Mapping KITSearch JSON Object");
			JsonNode obj = new ObjectMapper().readTree(json);
			JsonNode kitsearch = obj.get("KITSearch");
			Iterator<String> fieldNames = kitsearch.fieldNames();
			while(fieldNames.hasNext()){
				String fieldName = fieldNames.next();				
				List<JsonNode> fieldValues = kitsearch.findValues(fieldName);
				JsonNode values = fieldValues.get(0);
				for (JsonNode value : values) {
					builder.add(value.asText(), scoref.newScore(1.0)); //TODO: request KITSearch to include confidence value
				}
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.trace("List of media items URLs resources obtained: " + builder.toString());
		return builder.build();
	}
}
