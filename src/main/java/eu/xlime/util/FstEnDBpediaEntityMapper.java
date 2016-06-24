package eu.xlime.util;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.ImmutableSet;

public class FstEnDBpediaEntityMapper extends BaseEnDBpedKBEntityMapper {

	FstKBEntityMapper delegate;
	
	public FstEnDBpediaEntityMapper(File dbpInterlangTtlFile, File supportedOtherUrisFile) {
		delegate = new FstKBEntityMapper(dbpInterlangTtlFile, supportedOtherUrisFile);
	}
	
	@Override
	protected Set<String> getDBpediaSameAsSet(String entUri)
			throws ExecutionException {
		return ImmutableSet.<String>builder().addAll(delegate.expandSameAs(entUri)).add(entUri).build();
	}

}
