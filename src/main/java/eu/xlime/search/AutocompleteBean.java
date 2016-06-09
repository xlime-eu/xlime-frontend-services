package eu.xlime.search;

import java.io.Serializable;
import java.util.List;

import eu.xlime.bean.UrlLabel;

/**
 * Represents the autocomplete entities of a given {@link #keyword}.
 * 
 * @author Nuria Garcia
 * @email ngarcia@expertsystem.com
 *
 */

public class AutocompleteBean implements Serializable {
	
	private static final long serialVersionUID = 6832764178337745890L;
	
	private List<UrlLabel> entities;
	
	public List<UrlLabel> getEntities() {
		return entities;
	}

	public void setEntities(List<UrlLabel> entities) {
		this.entities = entities;
	}

	@Override
	public String toString() {
		return "AutocompleteBean [entities=" + entities + "]";
	}	

}
