package eu.xlime.mongo;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Properties;

import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
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
	
	@Test
	public void testSearchByDate() throws Exception {
		Properties props = new Properties();
		//assume default configuration (empty props)
		DBCollectionProvider cp = new DBCollectionProvider(props);
		Class<MicroPostBean> clz = MicroPostBean.class;
		DBCollection coll = cp.getDBCollection(clz);
		JacksonDBCollection<MicroPostBean, String> jacksonColl = JacksonDBCollection.wrap(coll, clz,
		        String.class);
		
		ISO8601DateFormat format = new ISO8601DateFormat();
		Date d = format.parse("2016-10-08T11:53:02Z");
		DBCursor<MicroPostBean> cursor = jacksonColl.find().lessThanEquals("created.timestamp", d);
		System.out.println("Cursor: " + cursor.getQuery());
		assertEquals(2, cursor.count());
	}
}
