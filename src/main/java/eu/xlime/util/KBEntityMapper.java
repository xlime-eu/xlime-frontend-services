package eu.xlime.util;

import java.util.Set;

import com.google.common.base.Optional;

/**
 * Interface for a component which is able to canonicalise (and expand) KBEntity uris.
 * 
 * @author rdenaux
 *
 */
public interface KBEntityMapper {

	public abstract Optional<String> toCanonicalEntityUrl(String entUrl);

	public abstract Set<String> expandSameAs(String entUrl);

}