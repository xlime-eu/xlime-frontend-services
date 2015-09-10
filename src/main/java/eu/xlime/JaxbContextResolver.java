package eu.xlime;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.SingleMediaItemBean;

@Provider 
public class JaxbContextResolver implements ContextResolver<JAXBContext> {

	private JAXBContext context;

	private Class[] types = {
			MicroPostBean.class,
			SingleMediaItemBean.class,
	};	
	
	public JaxbContextResolver() throws Exception {
		super();
		this.context = new JSONJAXBContext(JSONConfiguration.mapped().arrays().build(), types);
	}


	@Override
	public JAXBContext getContext(Class<?> arg0) {
		return context;
	}

}
