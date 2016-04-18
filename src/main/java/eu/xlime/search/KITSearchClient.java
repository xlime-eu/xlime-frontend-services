package eu.xlime.search;

import java.io.File;
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

import com.blogspot.mydailyjava.guava.cache.overflow.FileSystemCacheBuilder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;

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

	/**
	 * Cache for recently retrieved kitsearch{@link Model}. Avoids having to call 
	 * the kitsearch server too often when requesting known keywords.
	 */
	private static Cache<String, List<String>> kitsearchModelCache = FileSystemCacheBuilder.newBuilder()
			.maximumSize(1L) // In-memory, rest goes to disk
			.persistenceDirectory(new File("target/kitsearchModelCache/"))
			.softValues()
			.build();

	public Optional<List<String>> retrieveKitsearchModel(final String keywords) {
		Callable<? extends List<String>> valueLoader = new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				return retrieveKitsearchModelFromServer(keywords).get();
			}
		};

		try {
			return Optional.of(kitsearchModelCache.get(keywords, valueLoader));
		} catch (ExecutionException e) {
			log.warn("Error loading  kitsearchModel for " + keywords, e);
			return Optional.absent();
		}
	}

	public  Optional<List<String>> retrieveKitsearchModelFromServer(String keywords){
		log.debug("Retrieving KITSearch Model for " + keywords);
		Client client = ClientBuilder.newClient();
		WebTarget kitsearch = client.target("http://km.aifb.kit.edu/services/xlimesearch");
		WebTarget qsearch = kitsearch.path("kitsearch");

		WebTarget target = qsearch.queryParam("q", keywords);

		Invocation.Builder invocationBuilder = target.request(textJson);

		Response resp = invocationBuilder.get();
		int status = resp.getStatus();
		log.debug("Response status: " + status);
		if (status != 200) {
			log.debug("Error retrieving KITSearch " + resp.getStatusInfo());
			return Optional.absent();
		}
		log.trace("response has entity: " + resp.hasEntity());
		if (resp.hasEntity()) {
			final String json = resp.readEntity(String.class);
			log.trace("Resp entity: " + json);
			return toAutocomplete(json);
		} else {
			return Optional.absent();
		}		
	}

	public Optional<List<String>> retrieveKitsearch(String keywords) {
		return retrieveKitsearchModel(keywords);		
	}

	private Optional<List<String>> toAutocomplete(String json) {
		List<String> result = new ArrayList<String>();
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
					result.add(value.asText());
				}
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.trace("List of media items URLs resources obtained: " + result.toString());
		return Optional.of(result);
	}
}
