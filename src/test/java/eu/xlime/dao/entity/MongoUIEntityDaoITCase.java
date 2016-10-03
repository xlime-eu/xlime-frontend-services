package eu.xlime.dao.entity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Optional;

import eu.xlime.mongo.ConfigOptions;
import eu.xlime.summa.bean.UIEntity;

public class MongoUIEntityDaoITCase {

	//TODO: make sure test mongo db is set-up for this test
	
	@Test
	@Ignore("Setup mongodb test")
	public void test_retrieveFromUri() throws Exception {
		MongoUIEntityDao dao = createTestObj();
		Optional<UIEntity> ent = dao.retrieveFromUri("http://dbpedia.org/resource/Lúcia_Santos");
		assertNotNull(ent);
		System.out.println("Found ent " + ent);
		assertTrue(ent.isPresent());
	}
	
	private MongoUIEntityDao createTestObj() {
		Properties props = new Properties();
		props.put(ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getKey(), "xlimeres");
		MongoUIEntityDao dao = new MongoUIEntityDao(props);
		return dao;
	}
	
}
