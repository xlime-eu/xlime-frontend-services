package eu.xlime;

import static org.junit.Assert.*;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
//import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.xlime.bean.MediaItemListBean;

public class ServicesResourceJerseyTest extends JerseyTest {

    @Override
    protected Application configure() {
    	/*
        return new ResourceConfig(ServicesResource.class).
        		register(MoxyJsonFeature.class).
        		register(JsonMoxyConfigurationContextResolver.class);
        		*/
        return new ResourceConfig(ServicesResource.class).
        		addProperties(ImmutableMap.<String, Object>of("jersey.config.server.tracing.type", "ALL",
        				"jersey.config.server.tracing.threshold", "TRACE")).
        		register(JacksonFeature.class).
        		register(JacksonObjectMapperProvider.class);
    }	
    
	@Test
	public void testMediaItemSocMedia() throws Exception {
		WebTarget res = target("services/mediaItem").queryParam("url", "http://vico-research.com/social/056eeb12-6a21-38af-b40c-94fbabe8628f");
		
		Response response = res.request().get();
		System.out.println("Response: " + response);
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		System.out.println(response.getLength());
		String resp = res.request().get(String.class);
		System.out.println(resp);
		assertNotNull(resp);
	}

	@Test
	public void testMediaItemNews() throws Exception {
		WebTarget res = target("services/mediaItem").queryParam("url", "http://ijs.si/article/367691732");
		
		Response response = res.request().get();
		System.out.println("Response: " + response);
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		System.out.println(response.getLength());
		String resp = res.request().get(String.class);
		System.out.println(resp);
		assertNotNull(resp);
	}

	@Test
	public void testMediaItemNoUrl() throws Exception {
		WebTarget res = target("services/mediaItem");
		
		Response response = res.request().get();
		System.out.println("Response: " + response);
		assertEquals(500, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		System.out.println(response.getLength());
	}
	
	@Test
	public void testMediaItemInvalidRes() throws Exception {
		WebTarget res = target("services/media-invalid-item");
		Response response = res.queryParam("url", "http://vico-research.com/social/056eeb12-6a21-38af-b40c-94fbabe8628f").request().get();
		System.out.println("Response: " + response);
		assertEquals(404, response.getStatus());
	}

	@Test
	public void testLatestMediaItems() throws Exception {
		WebTarget res = target("services/latestMediaItems");
		Response response = res.queryParam("minutes", "1000").request().get();
		System.out.println("Response: " + response);
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		System.out.println(response.getLength());
		System.out.println("has entity: " + response.hasEntity());
		MediaItemListBean resp = response.readEntity(MediaItemListBean.class);
//		MediaItemListBean resp = res.request().get(MediaItemListBean.class);
		System.out.println("MediaItemListBean with " + resp.getMediaItems().size() + " media items and " + resp.getErrors().size() + " errors.");
		System.out.println(resp);
		assertNotNull(resp);
	}
	
}
