package eu.xlime;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class PubServicesInitializer implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent ctx) {
		System.out.println("TODO: release resources for " + ctx);
	}

	@Override
	public void contextInitialized(ServletContextEvent ctx) {
		System.out.println("TODO: init resources for " + ctx);		
	}

}
