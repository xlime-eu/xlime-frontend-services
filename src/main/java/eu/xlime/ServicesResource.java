package eu.xlime;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MediaItemListBean;
import eu.xlime.bean.SearchResultBean;
import eu.xlime.bean.UrlLabel;
import eu.xlime.dao.MediaItemAnnotationDao;
import eu.xlime.dao.MediaItemDao;
import eu.xlime.dao.MediaItemDaoImpl;
import eu.xlime.search.SearchItemDao;
import eu.xlime.sphere.SpheresFactory;
import eu.xlime.sphere.bean.Spheres;
import eu.xlime.summa.SummaClient;
import eu.xlime.summa.UIEntityFactory;
import eu.xlime.summa.bean.EntitySummary;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.score.ScoredSet;

/**
 * Provides the REST services for this web application.
 * 
 * @author RDENAUX
 *
 */
@Path("/services")
public class ServicesResource {

	private static final Logger log = LoggerFactory.getLogger(ServicesResource.class);
	
	private static final MediaItemDao mediaItemDao = new MediaItemDaoImpl();
	private static final MediaItemAnnotationDao mediaItemAnnotationDao = new MediaItemAnnotationDao();
	private static final SummaClient summaClient = new SummaClient();
	private static final SpheresFactory spheresFactory = new SpheresFactory();	
	private static final SearchItemDao searchItemDao = new SearchItemDao();	

	public ServicesResource() {
		log.info("Created " + this.getClass().getSimpleName());
	}
	
	@GET
	@Path("/mediaItem")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response mediaItem(@QueryParam("url") List<String> urls) {
		log.info("Received " + urls);
		MediaItemListBean milb = lookupMediaItems(urls);
		if (milb.getMediaItems().isEmpty() && !milb.getErrors().isEmpty()) {
			Response errorResponse = Response.serverError().entity(milb.getErrors()).build();
			return errorResponse;
		}
		String msg = String.format("Returning response for %s with %s media-items and %s errors.", urls, milb.getMediaItems().size(), milb.getErrors().size()); 
		log.info(msg);

		return Response.ok(milb).build();
	}

	private MediaItemListBean lookupMediaItems(List<String> urls) {
		MediaItemListBean milb = new MediaItemListBean();
		if (urls == null || urls.isEmpty()) milb.addError("No requested urls");
		for (String url: urls){
			try {
				Optional<? extends MediaItem> optMedItem = findMediaItem(url); 
				if (optMedItem.isPresent()) {
					milb.addMediaItem(optMedItem.get());
				} else {
					milb.addError("Failed to find media-item for " + url);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				milb.addError(e1.getLocalizedMessage());
			} 
		}
		return milb;
	}
	
	@GET
	@Path("/spheres")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response spheres(@QueryParam("context") List<String> context) {
		log.info("Received spheres?context=" + context);
		Spheres spheres = spheresFactory.buildSpheres(context);
		spheres.setUri("http://showcase.xlime.eu/spheres?context=" + context);
		spheres.setName("Showcase Spheres");
		spheres.setType("http://showcase.xlime.eu/sphere");
		return Response.ok(spheres).build();
	}
	
	@GET
	@Path("/latestMediaItems")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response latestMediaItems(@QueryParam("minutes") int minutes) {
		log.info("Received latestMediaItems?minutes=" + minutes);
		int limit = 50;
		if (minutes <= 0) minutes = 5;
		List<String> urls = mediaItemDao.findLatestMediaItemUrls(minutes, limit);
		return mediaItem(urls);
	}
	
	@GET
	@Path("/entities")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response entities(@QueryParam("url") List<String> urls) {
		log.info("Received " + urls);
		List<EntitySummary> list = new ArrayList<EntitySummary>();
		if (urls == null || urls.isEmpty()) 
			return Response.serverError().entity("No requested urls").build();
		for (String url: urls){
			try {
				Optional<? extends EntitySummary> optEntitySummary = findUIEntitySummary(url); 
				if (!optEntitySummary.isPresent()) throw new RuntimeException("Failed to find a KB entity for " + url);
				list.add(optEntitySummary.get());
			} catch (Exception e1) {
				e1.printStackTrace(); 
//				milb.addError(e1.getLocalizedMessage()); //gets ignored
				throw e1;
			} 
		}
		String msg = "Returning response for " + list; 
		log.info(msg);
//		System.out.println(msg);
		return Response.ok(list).build();
	}
	
	/**
	 * Returns a list of annotations for the input media-items.
	 * 
	 * @param urls
	 * @return
	 */
	@GET
	@Path("/annotationsFor")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response annotationsFor(@QueryParam("url") List<String> urls) {
		log.info("Received annotatationsFor with urls " + urls);
		MediaItemListBean milb = new MediaItemListBean();
		if (urls == null || urls.isEmpty()) milb.addError("No requested urls");
		List<EntityAnnotation> list = new ArrayList<>();
		for (String url: urls){
			try {
				list.addAll(findAnnotations(url));
			} catch (Exception e1) {
				e1.printStackTrace();
				throw e1;
			}
		}
		return Response.ok(list).build();
	}	

	/**
	 * Returns a list of media items and/or known KBentities from an input query (input: text or KB entity)
	 * 
	 * @param query
	 * @return
	 */
	@GET
	@Path("/search")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response search(@QueryParam("q") String query) {
		log.info("Received " + query);
		if (query == null || query.isEmpty()) 
			return Response.serverError().entity("No requested query").build();
		
		List<String> foundMedItemUrls = findMediaItemUrls(query).asList();
		List<UIEntity> ents = findEntities(query);
		
		MediaItemListBean milb = lookupMediaItems(foundMedItemUrls);
		
		SearchResultBean bean = new SearchResultBean();
		bean.getErrors().addAll(milb.getErrors());
		bean.getMediaItems().addAll(milb.getMediaItems());
		bean.getEntities().addAll(ents);
		
		return Response.ok(bean).build();
	}

	private List<UIEntity> findEntities(String query) {
		List<UrlLabel> urlLabels = searchItemDao.autoCompleteEntities(query);
		List<UIEntity> ents = new ArrayList<>();
		if (!urlLabels.isEmpty()) for (UrlLabel ul: urlLabels.subList(0, Math.min(5, urlLabels.size()))) {
			ents.add(UIEntityFactory.instance.retrieveFromUri(ul.getUrl()));
		}
		return ents;
	}

	private ScoredSet<String> findMediaItemUrls(String query) {
		ScoredSet<String> foundMedItemUrls;
		if(query.startsWith("http://"))			
			foundMedItemUrls = searchItemDao.findMediaItemUrlsByKBEntity(query);
		else
			foundMedItemUrls = searchItemDao.findMediaItemUrlsByText(query);
		return foundMedItemUrls;
	}

	private Optional<? extends EntitySummary> findUIEntitySummary(String url) {
		return summaClient.retrieveSummary(url);
	}

	final Optional<? extends MediaItem> findMediaItem(String url) {
		return mediaItemDao.findMediaItem(url);
	}
	
	final List<EntityAnnotation> findAnnotations(String url) {
		return mediaItemAnnotationDao.findMediaItemEntityAnnotations(url);
	}
	
}
