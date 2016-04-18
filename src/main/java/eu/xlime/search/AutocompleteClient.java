package eu.xlime.search;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
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
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;

/**
 * Java client implementation for the <a href="http://km.aifb.kit.edu/services/xlime-autocomplete">Autocomplete Service</a>.
 * 
 * @author Nuria Garcia 
 * @email ngarcia@expertsystem.com
 *
 */

public class AutocompleteClient {

	private static Logger log = LoggerFactory.getLogger(AutocompleteClient.class);

	private static MediaType textJson = new MediaType("text", "json");

	/**
	 * Cache for recently retrieved autocomplete{@link Model}. Avoids having to call 
	 * the autocomplete server too often when requesting known keywords.
	 */
	private static Cache<String, AutocompleteBean> autocompleteModelCache = FileSystemCacheBuilder.newBuilder()
			.maximumSize(1L) // In-memory, rest goes to disk
			.persistenceDirectory(new File("target/autocompleteModelCache/"))
			.softValues()
			.build();

	public Optional<AutocompleteBean> retrieveAutocompleteModel(final String keywords) {
		Callable<? extends AutocompleteBean> valueLoader = new Callable<AutocompleteBean>() {
			@Override
			public AutocompleteBean call() throws Exception {
				return retrieveAutocompleteModelFromServer(keywords).get();
			}
		};

		try {
			return Optional.of(autocompleteModelCache.get(keywords, valueLoader));
		} catch (ExecutionException e) {
			log.warn("Error loading autocompleteModel for " + keywords, e);
			return Optional.absent();
		}
	}

	public Optional<AutocompleteBean> retrieveAutocompleteModelFromServer(String keywords) {
		log.debug("Retrieving Autocomplete Model for " + keywords);
		Client client = ClientBuilder.newClient();
		WebTarget autocomplete = client.target("http://km.aifb.kit.edu/services/xlime-autocomplete");
		WebTarget auto = autocomplete.path("auto");

		WebTarget target = auto.queryParam("term", keywords);

		Invocation.Builder invocationBuilder = target.request(textJson);

		Response resp = invocationBuilder.get();
		int status = resp.getStatus();
		log.debug("Response status: " + status);
		if (status != 200) {
			log.debug("Error retrieving autocomplete " + resp.getStatusInfo());
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

	public Optional<AutocompleteBean> retrieveAutocomplete(String keywords) {
		return retrieveAutocompleteModel(keywords);		
	}

	private Optional<AutocompleteBean> toAutocomplete(String json) {
		JsonFactory jfactory = new JsonFactory();
		AutocompleteBean autocomplete = new AutocompleteBean();
		LinkedHashMap<String, String> entities = new LinkedHashMap<String, String>();
		try {
			JsonParser jParser = jfactory.createParser(json);
			JsonToken token = jParser.nextToken();
			if (!JsonToken.START_ARRAY.equals(token)) {
				log.debug("JSON Object not start with an array");
				return Optional.absent();
			}			
			log.trace("Mapping JSON Object to AutocompleteBean");
			while (token != JsonToken.END_ARRAY) {
				token = jParser.nextToken();
				jParser.nextToken();
				String label = jParser.nextTextValue();
				if(label != null){
					String[] entity = label.split("#");
					entities.put(entity[0], entity[1]);					
				}
				jParser.nextToken();
			}			
			autocomplete.setEntities(entities);
			autocomplete.setFirst_entity(entities.entrySet().iterator().next().getValue());
			log.trace("AutoComplete Object created:" + autocomplete.toString());
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return Optional.of(autocomplete);
	}

	/*public static void main(String[] args){
		//AutocompleteClient client = new AutocompleteClient();
		//client.retrieveAutocomplete("refugee");
		
		//SearchItemDao search = new SearchItemDao();
		//search.findMediaItemUrlsByKBEntity("http://dbpedia.org/resource/Refugee");
		//search.findMediaItemUrlsByText("refugee");
		
		//KITSearchClient kitclient = new KITSearchClient();
		//kitclient.retrieveKitsearch("refugee Greece");
		
		SearchItemDao search = new SearchItemDao();
		search.findMediaItemUrlsByFreeText("refugee Greece");
	}*/
}
