package eu.xlime;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.google.common.collect.ImmutableMap;

public class ServicesApp extends ResourceConfig {

	public ServicesApp() {
		packages("eu.xlime", "eu.xlime.bean");
		addProperties(ImmutableMap.<String, Object>of("jersey.config.server.tracing.type", "ALL",
				"jersey.config.server.tracing.threshold", "TRACE"));
		register(JacksonObjectMapperProvider.class);
		register(JacksonFeature.class);
	}
}
