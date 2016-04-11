package eu.xlime;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonObjectMapperProvider implements
		ContextResolver<ObjectMapper> {

    final ObjectMapper defaultObjectMapper;
    
    public JacksonObjectMapperProvider() {
    	defaultObjectMapper = createDefaultMapper();
	}
    
	private ObjectMapper createDefaultMapper() {
        final ObjectMapper result = new ObjectMapper();
//        result.configure(Feature.INDENT_OUTPUT, true);
//        result.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return result;
	}
	
	@Override
	public ObjectMapper getContext(Class<?> type) {
		return defaultObjectMapper;
	}

}
