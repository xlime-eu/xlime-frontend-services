package eu.xlime.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.mydailyjava.guava.cache.overflow.FileSystemCacheBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import eu.xlime.Config;

public class CacheFactory {

	private static Logger log = LoggerFactory.getLogger(CacheFactory.class);
	
	/**
	 * The instance of {@link CacheFactory} to be used by the rest of the application
	 */
	public static CacheFactory instance = new CacheFactory();
	
	/**
	 * Private constructor used to create {@link #instance}
	 */
	private CacheFactory() {
		//used to create instance
	}
	
	/**
	 * Builds a cache for the specified cacheName, taking into account
	 * the {@link Config}uration options (and defaults).
	 * 
	 * @param cacheName
	 * @return
	 */
	public <K,V> Cache<K,V> buildCache(String cacheName) {
		try {
			return buildPersistingCache(cacheName);
		} catch (Exception e) {
			log.error("Error creating persisting cache. Using in-memory instead.", e);
			return buildInMemoryCache(cacheName);
		}
	}

	private <K,V> Cache<K, V> buildInMemoryCache(String cacheName) {
		Config cfg = new Config();
		long maxSize = cfg.getLong(Config.Opt.CacheMaxSize);
		return CacheBuilder.newBuilder()
				.maximumSize(maxSize)
				.softValues()
				.build();
	}

	private <K,V> Cache<K, V> buildPersistingCache(String cacheName) {
		Config cfg = new Config();
		long maxSize = cfg.getLong(Config.Opt.CacheMaxSize);
		File baseCacheDir = new File(cfg.get(Config.Opt.CacheDir));
		File cacheDir = new File(baseCacheDir, cacheName);
		return FileSystemCacheBuilder.newBuilder()
				.maximumSize(maxSize) // In-memory, rest goes to disk
				.persistenceDirectory(cacheDir)
				.softValues()
				.build();		
	}
}
