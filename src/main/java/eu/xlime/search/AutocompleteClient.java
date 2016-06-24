package eu.xlime.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;

import eu.xlime.Config;
import eu.xlime.bean.UrlLabel;
import eu.xlime.util.CacheFactory;

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
	private static Cache<String, AutocompleteBean> autocompleteModelCache = CacheFactory.instance.buildCache("autocompleteModelCache");

	public Optional<AutocompleteBean> retrieveAutocompleteModel(final String keywords) {
		Callable<? extends AutocompleteBean> valueLoader = new Callable<AutocompleteBean>() {
			@Override
			public AutocompleteBean call() throws Exception {
				return retrieveAutocompleteModelFromServer(keywords).get();
			}
		};

		long start = System.currentTimeMillis();
		try {
			return Optional.of(autocompleteModelCache.get(keywords, valueLoader));
		} catch (ExecutionException e) {
			log.warn("Error loading autocompleteModel for " + keywords, e);
			return Optional.absent();
		} finally {
			log.debug(String.format("Executed retrieveAutocompleteModel in %s ms", (System.currentTimeMillis() - start)));
		}
	}

	public Optional<AutocompleteBean> retrieveAutocompleteModelFromServer(String keywords) {
		log.debug("Retrieving Autocomplete Model for " + keywords);
		Config cfg = new Config();
		Client client = ClientBuilder.newClient();
		WebTarget autocomplete = client.target(cfg.get(Config.Opt.AutocompleteUrl));
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
		List<UrlLabel> entities = new ArrayList<>();
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
					entities.add(decodeUrlLabel(label));
				}
				jParser.nextToken();
			}			
			autocomplete.setEntities(entities);
			if (log.isTraceEnabled()) {
				log.trace("AutoComplete Object created:" + autocomplete.toString());
			}
		} catch (JsonParseException e) {
			log.error("Error parsing autocomplete result", e);
			return Optional.absent();
		} catch (IOException e) {
			log.error("Error getting autocomplete result", e);
			return Optional.absent();
		}		
		return Optional.of(autocomplete);
	}

	/**
	 * Converts a line output by the <code>autocomplete</code> service to a {@link UrlLabel}.
	 * The input <code>line</code> must have the format 
	 * <pre>
	 *   LABEL '#' URL
	 * </pre>
	 * 
	 * @param line
	 * @return
	 */
	private UrlLabel decodeUrlLabel(String line) {
		String[] entity = line.split("#");
		UrlLabel urlLabel = new UrlLabel();
		urlLabel.setLabel(entity[0]);
		urlLabel.setUrl(entity[1]);
		return urlLabel;
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
