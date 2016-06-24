package eu.xlime.dao;

import java.util.List;
import java.util.Locale;

import com.google.common.base.Optional;

import eu.xlime.bean.UrlLabel;
import eu.xlime.summa.bean.UIEntity;

public interface UIEntityDao {

	/**
	 * Retrieves a {@link UIEntity} for a given entity URI. Implementing classes may only support 
	 * certain types of {@link UIEntity} (e.g. only entities from a certain knowledge base, or only 
	 * those from certain language branches of a knowledge base).
	 * @param entUri
	 * @return
	 */
	Optional<UIEntity> retrieveFromUri(String entUri);

	Optional<UIEntity> retrieveFromUri(String entUri, Optional<Locale> locale);
	
	List<UIEntity> retrieveFromUris(List<String> uris);

	List<UIEntity> retrieveFromUris(List<String> uris, Optional<Locale> locale);

	/**
	 * Returns a list of most likely {@link UIEntity}s (as {@link UrlLabel}s) for a given
	 * text.
	 *  
	 * @param text
	 * @return
	 */
	List<UrlLabel> autoCompleteEntities(String text);

}