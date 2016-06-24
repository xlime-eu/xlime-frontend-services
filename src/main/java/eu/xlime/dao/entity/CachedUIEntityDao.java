package eu.xlime.dao.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;

import eu.xlime.bean.UrlLabel;
import eu.xlime.dao.UIEntityDao;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.CacheFactory;

public class CachedUIEntityDao extends AbstractUIEntityDao {

	public static final Logger log = LoggerFactory.getLogger(CachedUIEntityDao.class.getName());
	
	private static Cache<String, UIEntity> uiEntityCache = CacheFactory.instance.buildCache("uiEntityCache");

	private final UIEntityDao delegate;
	
	public CachedUIEntityDao(UIEntityDao delegate) {
		super();
		this.delegate = delegate;
	}

	
	@Override
	public Optional<UIEntity> retrieveFromUri(final String entUri, final Optional<Locale> locale) {
		Callable<? extends UIEntity> valueLoader = new Callable<UIEntity>() {

			@Override
			public UIEntity call() throws Exception {
				try {
					return delegate.retrieveFromUri(entUri, locale).get();
				} catch (Exception e) {
					throw new ExecutionException(e);
				}
			}
		};
		try {
			return Optional.of(uiEntityCache.get(entUri, valueLoader));
		} catch (ExecutionException e) {
			log.error(String.format("Failed to retrieve UIEntity for %s in %s", entUri, locale), e);
			return Optional.absent();
		}
	}


	@Override
	public List<UIEntity> retrieveFromUris(List<String> uris,
			Optional<Locale> locale) {
		Map<String, UIEntity> cached = uiEntityCache.getAllPresent(uris);
		List<String> toFind = new ArrayList(uris);
		toFind.removeAll(cached.keySet());
		
		Map<String, UIEntity> nonCached = asMap(delegate.retrieveFromUris(toFind, locale));
		uiEntityCache.putAll(nonCached);
		
		log.debug(String.format("Found %s cached and %s uncached UIEntitys", cached.size(), nonCached.size()));
		
		return ImmutableList.<UIEntity>builder()
				.addAll(cleanEntities(ImmutableList.copyOf(cached.values())))
				.addAll(nonCached.values())
				.build();
	}

	@Override
	public List<UrlLabel> autoCompleteEntities(String text) {
		return delegate.autoCompleteEntities(text); //no caching for the autocompletions
	}


	@Override
	protected Locale getDefaultLocale() {
		if (delegate instanceof AbstractUIEntityDao) {
			return ((AbstractUIEntityDao)delegate).getDefaultLocale();
		} else return Locale.UK;
	}

	private Map<String, UIEntity> asMap(List<UIEntity> ents) {
		Map<String, UIEntity> result = new HashMap<String, UIEntity>();
		for (UIEntity ent: ents) {
			result.put(ent.getUrl(), ent);
		}
		return result;
	}

}
