package eu.xlime.util;

import java.util.Set;

import com.google.common.base.Optional;

public interface KBEntityMapper {

	public abstract Optional<String> toCanonicalEntityUrl(String entUrl);

	public abstract Set<String> expandSameAs(String entUrl);

}