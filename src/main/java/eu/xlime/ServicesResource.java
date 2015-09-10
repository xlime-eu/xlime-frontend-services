package eu.xlime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.SingleMediaItemBean;

@Path("/services")
public class ServicesResource {

	private static final Logger log = LoggerFactory.getLogger(ServicesResource.class);
	
	private static final MediaItemDao mediaItemDao = new MediaItemDao();
	
	@GET
	@Path("/mediaItem")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response mediaItem(@QueryParam("url") String url) {
		Response errorResponse = null;
		SingleMediaItemBean smib = null;
		try {
			smib = asSingleMediaItem(mediaItemDao.findMicroPost(url));
		} catch (Exception e1) {
			e1.printStackTrace();
			errorResponse = Response.serverError().entity(e1.getLocalizedMessage()).build();
		}
		if (errorResponse != null) return errorResponse;
		return Response.ok(smib).build();
	}

	private SingleMediaItemBean asSingleMediaItem(MicroPostBean microPost) {
		SingleMediaItemBean result = new SingleMediaItemBean();
		result.setMediaItem(microPost);
		return result;
	}
	
}
