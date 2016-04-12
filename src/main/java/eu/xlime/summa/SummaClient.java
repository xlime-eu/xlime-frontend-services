package eu.xlime.summa;

import java.io.File;
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

import com.blogspot.mydailyjava.guava.cache.overflow.FileSystemCacheBuilder;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;

import eu.xlime.summa.bean.EntitySummary;

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
	private static Cache<String, Model> summaModelCache = FileSystemCacheBuilder.newBuilder()
			.maximumSize(1L) // In-memory, rest goes to disk
			.persistenceDirectory(new File("target/summaModelCache/"))
			.softValues()
			.build();

	public Optional<Model> retrieveSummaryModel(final String entityUrl) {
		Callable<? extends Model> valueLoader = new Callable<Model>() {
			@Override
			public Model call() throws Exception {
				return retrieveSummaModelFromServer(entityUrl).get();
			}
		};
		
		try {
			return Optional.of(summaModelCache.get(entityUrl, valueLoader));
		} catch (ExecutionException e) {
			log.warn("Error loading summaModel for " + entityUrl, e);
			return Optional.absent();
		}
		
	}

	private Optional<Model> retrieveSummaModelFromServer(String entityUrl) {
		log.debug("Retrieving Summary Model for " + entityUrl);
		Client client = ClientBuilder.newClient();
		WebTarget summa = client.target("http://km.aifb.kit.edu/services/summa/");
		WebTarget summarum = summa.path("summarum");
		
		WebTarget target = summarum			
				.queryParam("entity", entityUrl)
				.queryParam("topK", 5);

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
			// TODO Auto-generated catch block
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
