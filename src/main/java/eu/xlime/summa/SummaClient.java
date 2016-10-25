package eu.xlime.summa;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import eu.xlime.Config;
import eu.xlime.summa.bean.EntitySummary;
import eu.xlime.util.CacheFactory;

/**
 * Java client implementation for the <a href="https://github.com/athalhammer/summaServer">Summa server</code>.
 *  
 * @author RDENAUX
 *
 */
public class SummaClient {
	
	private static Logger log = LoggerFactory.getLogger(SummaClient.class);
	
	private static MediaType textTurtle = new MediaType("text", "turtle");

	/**
	 * Cache for recently retrieved summa{@link Model}s. Avoids having to call 
	 * the summa server too often when requesting known entityUrls.
	 */
	private static Cache<String, Model> summaModelCache = CacheFactory.instance.buildCache("summaModelCache");
	
	public Optional<Model> retrieveSummaryModel(final String entityUrl) {
		Callable<? extends Model> valueLoader = new Callable<Model>() {
			@Override
			public Model call() throws Exception {
				Optional<Model> optMod = retrieveSummaModelFromServer(entityUrl);
				if (optMod.isPresent()) return optMod.get();
				else throw new ExecutionException("No response from server", null);
			}
		};
		
		try {
			return Optional.of(summaModelCache.get(entityUrl, valueLoader));
		} catch (UncheckedExecutionException e) {
			log.warn("Error loading summaModel for " + entityUrl, e);
			return Optional.absent();
		} catch (ExecutionException e) {
			log.warn("Error loading summaModel for " + entityUrl, e);
			return Optional.absent();
		}
		
	}

	private Optional<Model> retrieveSummaModelFromServer(String entityUrl) {
		log.debug("Retrieving Summary Model for " + entityUrl);
		Config cfg = new Config();
		String summaServerUrl = cfg.get(Config.Opt.SummaServerUrl);
		String summaServerPath = cfg.get(Config.Opt.SummaServerPath);
		int summaTopK = cfg.getInt(Config.Opt.SummaTopK);
		
		Client client = ClientBuilder.newClient();
		WebTarget summa = client.target(summaServerUrl);
		WebTarget summarum = summa.path(summaServerPath);
		
		WebTarget target = summarum			
				.queryParam("entity", entityUrl)
				.queryParam("topK", summaTopK);

		Invocation.Builder invocationBuilder = target.request(textTurtle);
		
		Response resp = invocationBuilder.get();
		int status = resp.getStatus();
		log.debug("Response status: " + status);
		if (status != 200) {
			log.debug("Error retrieving entity summary " + resp.getStatusInfo());
			return Optional.absent();
		}
		log.debug("response has entity: " + resp.hasEntity());
		if (resp.hasEntity()) {
			final String turtle = resp.readEntity(String.class);
			log.trace("Resp entity: " + turtle);
			return toRDFModel(turtle, target.getUri().toString());
		} else {
			return Optional.absent();
		}
	}
	
	public Optional<EntitySummary> retrieveSummary(String entityUrl) {
		Optional<Model> optModel = retrieveSummaryModel(entityUrl);
		if (optModel.isPresent()) {
			return toEntitySummary(optModel.get());
		} else return Optional.absent();
	}
	
	private Optional<Model> toRDFModel(String turtle, String baseUrl) {
		Model model = null;
		try {
			model = parseTurtle(turtle, baseUrl);
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			log.error("Error parsing turtle for entity summary", e);
			if (log.isTraceEnabled()) {
				log.trace("Parsed input " + turtle);
			}
			return Optional.absent();
		}
		return Optional.of(model);
	}

	private Optional<EntitySummary> toEntitySummary(Model model) {
		return new EntitySummaryFromRDFModelFactory(model).extractFromModel();
	}
		

	public Model parseTurtle(String input, String baseUri) throws RDFParseException, RDFHandlerException, IOException {
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		Model result = new LinkedHashModel();
		parser.setRDFHandler(new StatementCollector(result));
		
		parser.parse(new StringReader(input), baseUri);
		return result;
	}
}
