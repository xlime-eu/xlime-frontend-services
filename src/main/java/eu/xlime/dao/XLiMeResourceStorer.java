package eu.xlime.dao;

import java.util.Locale;

import com.google.common.base.Optional;

import eu.xlime.bean.XLiMeResource;

public interface XLiMeResourceStorer {

	/**
	 * Inserts (or replaces?) an {@link XLiMeResource} onto the back-end database.
	 *  
	 * @param xLiMeRes
	 * @return
	 */
	<T extends XLiMeResource> String insertOrUpdate(T xLiMeRes);

	<T extends XLiMeResource> String insertOrUpdate(T xLiMeRes, Optional<Locale> optLocale);
	
	/**
	 * Returns the number of {@link XLiMeResource}s of a given type are stored
	 * in the underlying database, assuming no {@link Locale}.
	 * 
	 * @param xLiMeResClass
	 * @return
	 */
	<T extends XLiMeResource> long count(Class<T> xLiMeResClass);

	<T extends XLiMeResource> long count(Class<T> xLiMeResClass, Optional<Locale> optLocale);
	
	<T extends XLiMeResource> Optional<T> findResource(Class<T> resourceClass, String resourceUrl);

	<T extends XLiMeResource> Optional<T> findResource(Class<T> resourceClass, Optional<Locale> optLocale, String resourceUrl);
	
}
