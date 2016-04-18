package eu.xlime.search;

import java.util.LinkedHashMap;

/**
 * Represents the autocomplete entities of a given {@link #keyword}.
 * 
 * @author Nuria Garcia
 * @email ngarcia@expertsystem.com
 *
 */

public class AutocompleteBean {
	
	private LinkedHashMap<String, String> entities;
	private String first_entity;
	
	public LinkedHashMap<String, String> getEntities() {
		return entities;
	}

	public void setEntities(LinkedHashMap<String, String> entities) {
		this.entities = entities;
	}	
	
	public String getFirst_entity() {
		return first_entity;
	}

	public void setFirst_entity(String first_entity) {
		this.first_entity = first_entity;
	}

	@Override
	public String toString() {
		return "Autocomplete: First entity:" + first_entity + " - [entities=" + entities + "]";
	}
}
