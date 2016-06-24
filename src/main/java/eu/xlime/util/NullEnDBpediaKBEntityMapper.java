package eu.xlime.util;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.ImmutableSet;

/**
 * Only provides basic mapping between wikipedia and dbpedia using string manipulation, no 
 * <code>owl:sameAs</code> expansion is performed.
 * 
 * @author rdenaux
 *
 */
public class NullEnDBpediaKBEntityMapper extends BaseEnDBpedKBEntityMapper {

	@Override
	protected Set<String> getDBpediaSameAsSet(String entUri)
			throws ExecutionException {
		return ImmutableSet.of(entUri);
	}

}
