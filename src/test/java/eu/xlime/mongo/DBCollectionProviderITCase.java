package eu.xlime.mongo;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import com.mongodb.DBCollection;

import eu.xlime.bean.MicroPostBean;

public class DBCollectionProviderITCase {

	@Test
	public void testCreate() {
		Properties props = new Properties();
		//assume default configuration (empty props)
		DBCollectionProvider cp = new DBCollectionProvider(props);
		assertNotNull(cp);
	}
	
	@Test
	public void testGetDBCollection() {
		Properties props = new Properties();
		//assume default configuration (empty props)
		DBCollectionProvider cp = new DBCollectionProvider(props);
		DBCollection coll = cp.getDBCollection(MicroPostBean.class);
		assertNotNull(coll);
		System.out.println("Micropost beans in mongo: " + coll.count());
		assertNotNull(coll);
	}
}
