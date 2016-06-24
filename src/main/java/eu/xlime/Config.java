package eu.xlime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * Provides configuration options
 * 
 * @author RDENAUX
 *
 */
public class Config {

	private static final Logger LOG = LoggerFactory.getLogger(Config.class);

	private static final String xLiMeCfgPath = "XLIME_CFG_PATH";
	private static final String cfgFileName = "xlime-ui-services.properties";
	/**
	 * Used to remember the last cfg path (to avoid logging the same path every time)
	 */
	private static String lastCfgPath = null;

	private final File configFile;
	
	private ReadProps readProps;
	
	private static class ReadProps {
		final long timeRead;
		final Properties props;
		public ReadProps(long timeRead, Properties props) {
			super();
			this.timeRead = timeRead;
			this.props = props;
		}
	}
	
	public enum Opt {
		SparqlEndpoint("xlime.sparql.endpoint.url"),
		SparqlUname("xlime.sparql.endpoint.username"),
		SparqlPassw("xlime.sparql.endpoint.password"),
		SparqlRate("xlime.sparql.endpoint.max-queries-per-second", "2.0"),
		SparqlTimeout("xlime.sparql.timeout", "5000"), 
		DBpediaSparqlEndpoint("xlime.sparql.dbpedia.endpoint.url", "http://dbpedia.org/sparql"),
		DBpediaSparqlRate("xlime.sparql.dbpedia.endpoint.max-queries-per-second", "1.0"),
		CacheMaxSize("xlime.cache.max-size", "500"),
		CacheDir("xlime.cache.dir", "xlime-front-end-cache/"),
		AutocompleteUrl("xlime.autocomplete.url", "http://km.aifb.kit.edu/services/xlime-autocomplete"), 
		SummaServerUrl("xlime.summa.server-url", "http://km.aifb.kit.edu/services/summa/"), 
		SummaServerPath("xlime.summa.server-path", "summarum"),
		SummaTopK("xlime.summa.topk", "5"), 
		XLiMeSearch("xlime.search.url", "http://km.aifb.kit.edu/services/xlimesearch");
		
		final String propKey;
		final Optional<String> defaultValue;
		
		private Opt(String propKey) {
			this(propKey, null);
		}
		
		private Opt(String propKey, String defaultValue) {
			this.propKey = propKey;
			this.defaultValue = Optional.fromNullable(defaultValue);
		}
	}
	
	public Config() {
		this(resolveConfigPropsFile());
	}
	
	Config(File configPropsFile) {
		configFile = configPropsFile;
	}

	public String get(Opt configOption) {
		Properties props = getCfgProps();
		return props.getProperty(configOption.propKey, configOption.defaultValue.orNull());
	}
	
	public int getInt(Opt configOption) {
		Properties props = getCfgProps();
		String strVal = props.getProperty(configOption.propKey);
		if (strVal != null) try {
			return Integer.valueOf(strVal);
		} catch (NumberFormatException e) {
			LOG.error("Value for " + configOption + " should be an integer. Using default value instead.", e);
		}
		if (configOption.defaultValue.isPresent())
			return Integer.valueOf(configOption.defaultValue.get());
		else throw new IllegalArgumentException("No valid configuration for " + configOption + " available.");
	}
	
	public long getLong(Opt configOption) {
		Properties props = getCfgProps();
		String strVal = props.getProperty(configOption.propKey);
		if (strVal != null) try {
			return Long.valueOf(strVal);
		} catch (NumberFormatException e) {
			LOG.error("Value for " + configOption + " should be a Long. Using default value instead.", e);
		}
		if (configOption.defaultValue.isPresent())
			return Long.valueOf(configOption.defaultValue.get());
		else throw new IllegalArgumentException("No valid configuration for " + configOption + " available.");
	}
	
	
	public double getDouble(Opt configOption) {
		Properties props = getCfgProps();
		String strVal = props.getProperty(configOption.propKey);
		if (strVal != null) try {
			return Double.valueOf(strVal);
		} catch (NumberFormatException e) {
			LOG.error("Value for " + configOption + " should be a double. Using default value instead.", e);
		}
		if (configOption.defaultValue.isPresent())
			return Double.valueOf(configOption.defaultValue.get());
		else throw new IllegalArgumentException("No valid configuration for " + configOption + " available.");
	}
	
	/**
	 * Gets the configuration properties based on the {@link #configFile}.
	 *  
	 * @return
	 */
	public Properties getCfgProps() {
		if (needsRefresh(readProps))
			readProps = readPropertiesFromConfigFile();

		if (readProps != null)
			return readProps.props;
		else return new Properties();
	}

	/**
	 * Determines whether a {@link ReadProps} is outdated, this is the case when 
	 * the {@link #configFile} has been modified after the readProps was read.
	 * 
	 * @param readProps
	 * @return
	 */
	private boolean needsRefresh(ReadProps readProps) {
		if (readProps == null) return true;
		if (configFile == null) return false;
		long lastMod = configFile.lastModified();
		if (lastMod == 0L) return false;
		else return readProps.timeRead < lastMod;
	}

	private ReadProps readPropertiesFromConfigFile() {
		Properties defaultProps = new Properties();
		if (configFile == null) return new ReadProps(System.currentTimeMillis(), defaultProps);
		FileInputStream in = null;
		try {
			in = new FileInputStream(configFile);
			defaultProps.load(in);
		} catch (IOException e) {
			e.printStackTrace();
			//ignore, defaultProps will be empty
		} finally {
			if (in != null) try {
				in.close();
			} catch (IOException e) {
				LOG.warn("Error closing inputstream from configFile.", e);
			}	
		}
		return new ReadProps(System.currentTimeMillis(), new Properties(defaultProps));
	}

	private static File resolveConfigPropsFile() {
		String cfgPath = System.getenv(xLiMeCfgPath);
		if (lastCfgPath == null || !lastCfgPath.equals(cfgPath)) {
			lastCfgPath = cfgPath;
			LOG.info("ConfigPath from sysenvs: " + cfgPath);
		}
		if (cfgPath == null) {
			File currentDir = new File("."); 
			LOG.debug("No " + xLiMeCfgPath + " available from sysenv:\n" + System.getenv() + " using current folder instead " + currentDir.getAbsolutePath());
			cfgPath = ".";
		}
		File cfgDir = new File(cfgPath);
		if (!cfgDir.exists()) {
			LOG.warn("Configuration path '" + cfgDir.getAbsolutePath() + "' does not exist. Check your configuration.");
			return null;
		}
		if (!cfgDir.isDirectory()) {
			LOG.warn("Configuration path '" + cfgDir.getAbsolutePath() + "' is not a folder. Check your configuration.");
			return null;
		}
		File propsFile = new File(cfgPath, cfgFileName);
		if (!propsFile.exists()) {
			LOG.warn("Custom configuration file " + propsFile.getAbsolutePath() + " does not exist. Check your configuration.");
			return null;
		}
		return propsFile;
	}
	
}
