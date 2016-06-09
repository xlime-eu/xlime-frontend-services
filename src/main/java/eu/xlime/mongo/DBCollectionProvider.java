package eu.xlime.mongo;

import java.util.Properties;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import eu.xlime.bean.XLiMeResource;

public class DBCollectionProvider {

	private static final Logger log = LoggerFactory.getLogger(DBCollectionProvider.class);
	
	private MongoClient client;
	private Properties cfgProps;
	private String dbName;
	
	public DBCollectionProvider(Properties props) {
		cfgProps = props;
		String connectionStr = ConfigOptions.XLIME_MONGO_CONNECTION_STRING.getValue(cfgProps);
		dbName = ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getValue(cfgProps);
		client = createClient(connectionStr);
		log.info("Succesfully created connection to mongo at " + connectionStr);
		if (log.isDebugEnabled()) {
			log.debug("Available databases: " + client.getDatabaseNames());
			log.debug(String.format("Resource database: '%s', exists? %s", dbName, client.getDatabaseNames().contains(dbName)));
		}
	}
	
	private MongoClient createClient(String connectionStr) {
		MongoClientURI clientUri = new MongoClientURI(connectionStr);
		return new MongoClient(clientUri);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
    protected void finalize() throws Throwable {
    	try {
    		client.close();
    	} finally {
        	super.finalize();
        }

    }	
	/**
	 * 
	 * @param beanClass
	 * @return
	 * @deprecated Mongo's API is deprecating {@link DBCollection}, migrate to {@link #getMongoCollection(Class)} instead
	 */
	public <T extends XLiMeResource> DBCollection getDBCollection(Class<T> beanClass) {
		DB db = client.getDB(dbName);
		return db.getCollection(beanClass.getSimpleName());
	}

	public <T extends XLiMeResource> MongoCollection<Document> getMongoCollection(Class<T> beanClass) {
		String dbName = ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getValue(cfgProps);
		MongoDatabase db = client.getDatabase(dbName);
		return db.getCollection(beanClass.getSimpleName());
	}
	
}
