package eu.xlime.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public enum ConfigOptions {

	XLIME_MONGO_CONNECTION_STRING(//
			"xlime.mongo.connection.string",
			"A connection uri for connecting to a MongoDB instance or cluster. Default is 'mongodb://localhost:27017'",
			"mongodb://localhost:27017", String.class), 
	XLIME_MONGO_RESOURCE_DATABASE_NAME(//
			"xlime.mongo.resource.database.name",
			"The name for the database used to store xlime resources. Default is 'xlimeres'",
			"xlimeres", String.class);
	
	final String key;

	final String description;

	final String defaultValue;

	final Class<?> type;

	private ConfigOptions(String aKey, String aDesc, String aDefaultValue,
			Class<?> aType) {
		key = aKey;
		description = aDesc;
		defaultValue = aDefaultValue;
		type = aType;
	}
	
	public Optional<String> getOptVal(Properties props, String... patternReplacements) {
		return Optional.fromNullable(getValue(props, patternReplacements));
	}
	
	public String getValue(Properties props, String... patternReplacements) {
		return props.getProperty(String.format(key, patternReplacements), defaultValue);
	}
	
	public String getKey(String... patternReplacements) {
		return String.format(key, patternReplacements);
	}
	
	public List<String> getList(Properties props) {
		String val = getValue(props);
		String[] vals = val.split(",");
		List<String> result = new ArrayList<String>();
		for (String v: vals) {
			result.add(v.trim());
		}
		return ImmutableList.copyOf(result);
	}
	
	public Boolean getBoolValue(Properties props, String... patternReplacements) {
		return Boolean.valueOf(getValue(props, patternReplacements));
	}
	
	public Integer getIntValue(Properties props, String... patternReplacements) {
		return Integer.valueOf(getValue(props, patternReplacements));
	}

	public Optional<Integer> getOptIntVal(Properties props, String... patternReplacements) {
		Optional<String> optVal = getOptVal(props, patternReplacements);
		if (optVal.isPresent()) {
			return Optional.of(Integer.valueOf(optVal.get()));
		} else return Optional.absent();
	}

	public Optional<Long> getOptLongVal(Properties props, String... patternReplacements) {
		Optional<String> optVal = getOptVal(props, patternReplacements);
		if (optVal.isPresent()) {
			return Optional.of(Long.valueOf(optVal.get()));
		} else return Optional.absent();
	}
	
}
