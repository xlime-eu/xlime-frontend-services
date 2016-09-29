package eu.xlime.datasum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import eu.xlime.Config;
import eu.xlime.datasum.bean.DatasetSummary;

public class CachedDatasetSummaryFactory implements DatasetSummaryFactory {

	private static final Logger log = LoggerFactory.getLogger(CachedDatasetSummaryFactory.class);
	
	public static final CachedDatasetSummaryFactory instance = new CachedDatasetSummaryFactory();

	private DatasetSummaryFactory delegate = new DatasetSummaryFactoryImpl();

	private DatasetSummary cachedSparqlSumma;
	private DatasetSummary cachedMongoSumma;
	private final Timer timer;
	
	private CachedDatasetSummaryFactory() {
		cachedSparqlSumma = retrieveFromDisk("sparql");
		cachedMongoSumma = retrieveFromDisk("mongo");
		timer = new Timer();
		Config cfg = new Config();
		
		int sparqlPeriodMin = cfg.getInt(Config.Opt.DatasetSummaSparqlCachePeriodMinutes);
		int mongoPeriodMin = cfg.getInt(Config.Opt.DatasetSummaMongoCachePeriodMinutes);
		long sparqlPeriod = sparqlPeriodMin * 60 * 1000;
		long mongoPeriod = mongoPeriodMin * 60 * 1000;
		
		timer.schedule(createSummaTimer("mongo"), calcDelay(cachedMongoSumma, mongoPeriod), mongoPeriod);		
		timer.schedule(createSummaTimer("sparql"), calcDelay(cachedSparqlSumma, sparqlPeriod), sparqlPeriod);
	}

	private DatasetSummary retrieveFromDisk(String dsName) {
		File f = calcCachedFile(dsName);
		return deserialize(f);
	}

	private DatasetSummary deserialize(File f) {
		DatasetSummary result = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(f);
			in = new ObjectInputStream(fis);
			result = (DatasetSummary) in.readObject();
			return result;
		} catch (IOException ex) {
			log.error("Failed to read bean from " + f.getAbsolutePath());
		} catch (ClassNotFoundException cnfe) {
			log.error("Failed to read bean from " + f.getAbsolutePath());			
		} finally {
			if (in != null) try { 
				in.close(); 
			} catch (Exception e) {
				//ignore
			}
		}
		return null;
	}

	private long calcDelay(DatasetSummary summa, long period) {
		if (summa == null) return 0;
		Date now = new Date();
		long age = now.getTime() - summa.getSummaryDate().getTime();
		if (age < 0) return period;
		if (age > period) return 0;
		return period - age;
	}

	private TimerTask createSummaTimer(final String dsName) {
		return new TimerTask() {

			@Override
			public void run() {
				long start = System.currentTimeMillis();
				refreshCachedSumma(dsName);
				long end = System.currentTimeMillis();
				log.info("Refreshed cache for dataset-summary of " +dsName + " in " + (end - start) + "ms." );
			}
			
		};
	}

	protected void refreshCachedSumma(String dsName) {
		if ("sparql".equalsIgnoreCase(dsName)) {
			try {
				log.info("Refreshing " + dsName + " old value: " + cachedSparqlSumma);
				cachedSparqlSumma = delegate.createXLiMeSparqlSummary();
				log.info("Refreshing " + dsName + " new value: " + cachedSparqlSumma);
				storeToDisk(dsName, cachedSparqlSumma);
			} catch (Exception e) {
				//ensure we keep the old value?
				log.trace("Failed to refresh cached summary for " + dsName, e);
			}
		} else if ("mongo".equalsIgnoreCase(dsName)) {
			try {
				log.info("Refreshing " + dsName + " old value: " + cachedMongoSumma);
				cachedMongoSumma = delegate.createXLiMeMongoSummary();
				log.info("Refreshing " + dsName + " new value: " + cachedMongoSumma);
				storeToDisk(dsName, cachedMongoSumma);
			} catch (Exception e) {
				//ensure we keep the old value?
				log.error("Failed to refresh cached summary for " + dsName, e);
			}
		}
	}

	private void storeToDisk(String dsName, DatasetSummary dsSumma) {
		if (dsSumma == null) return;
		File f = calcCachedFile(dsName);
	    serialize(dsSumma, f);
	}

	private File calcCachedFile(String dsName) {
		Config cfg = new Config();
		
		File f = new File(String.format("%s/datasetSum/%s.ser", cfg.get(Config.Opt.CacheDir), dsName));
		return f;
	}

	private void serialize(DatasetSummary dsSumma, File f) {
		FileOutputStream fos = null;
	    ObjectOutputStream out = null;
	    try {
	    	Files.createParentDirs(f);
	    	fos = new FileOutputStream(f);
	    	out = new ObjectOutputStream(fos);
	    	out.writeObject(dsSumma);

	    	out.close();
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    } finally {
        	if (out != null) try {
        		out.close();
        	} catch (Exception e) {
        		//ignore
        	}
	    }
	}

	@Override
	public DatasetSummary createXLiMeSparqlSummary() {
		if (cachedSparqlSumma == null) return emptyDatasetSumma();
		else return cachedSparqlSumma;
	}

	@Override
	public DatasetSummary createXLiMeMongoSummary() {
		if (cachedMongoSumma == null) return emptyDatasetSumma();
		return cachedMongoSumma;
	}

	private DatasetSummary emptyDatasetSumma() {
		DatasetSummary result = new DatasetSummary();
		result.setErrors(ImmutableList.of("Dataset not available"));
		return result;
	}
}
