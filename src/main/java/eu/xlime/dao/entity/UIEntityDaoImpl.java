package eu.xlime.dao.entity;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import eu.xlime.Config;
import eu.xlime.bean.UrlLabel;
import eu.xlime.dao.UIEntityDao;
import eu.xlime.summa.bean.UIEntity;

public class UIEntityDaoImpl implements UIEntityDao {

	public static final Logger log = LoggerFactory.getLogger(UIEntityDaoImpl.class.getName());
	
	public static final UIEntityDao instance = new UIEntityDaoImpl();
	
	public final UIEntityDao delegate;
	
	private UIEntityDaoImpl() {
//		delegate = new CachedUIEntityDao(new XLiMeSparqlUIEntityDao());
		delegate = new MongoUIEntityDao(new Config().getCfgProps());
	}
	
	/* (non-Javadoc)
	 * @see eu.xlime.summa.UIEntityFactory#retrieveFromUri(java.lang.String)
	 */
	@Override
	public Optional<UIEntity> retrieveFromUri(final String entUri) {
		return delegate.retrieveFromUri(entUri);
	}
	
	/* (non-Javadoc)
	 * @see eu.xlime.summa.UIEntityFactory#retrieveFromUris(java.util.List)
	 */
	@Override
	public List<UIEntity> retrieveFromUris(List<String> uris) {
		return delegate.retrieveFromUris(uris);
	}

	@Override
	public Optional<UIEntity> retrieveFromUri(String entUri, Optional<Locale> locale) {
		return delegate.retrieveFromUri(entUri, locale);
	}

	@Override
	public List<UrlLabel> autoCompleteEntities(String text) {
		return delegate.autoCompleteEntities(text);
	}

	@Override
	public List<UIEntity> retrieveFromUris(List<String> uris,
			Optional<Locale> locale) {
		return delegate.retrieveFromUris(uris, locale);
	}
	
}
