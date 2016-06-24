package eu.xlime.util;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * Provides services for mapping entity URIs from different KBs or different languages to
 * some <i>canonical entity</i>, usually by resolving <code>owl:sameAs</code> chains.
 *  
 * @author RDENAUX
 *
 */
public class KBEntityMapperImpl implements KBEntityMapper {
	
	private static final Logger log = LoggerFactory.getLogger(KBEntityMapperImpl.class);
	
	private KBEntityMapper delegate;

	public KBEntityMapperImpl() {
		super();
		this.delegate = new SparqlKBEntityMapper();
	}

	@Override
	public Optional<String> toCanonicalEntityUrl(String entUrl) {
		return delegate.toCanonicalEntityUrl(entUrl);
	}

	@Override
	public Set<String> expandSameAs(String entUrl) {
		return delegate.expandSameAs(entUrl);
	}
	
	
}
