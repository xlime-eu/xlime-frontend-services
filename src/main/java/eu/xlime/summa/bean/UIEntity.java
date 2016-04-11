package eu.xlime.summa.bean;

import java.io.Serializable;
import java.util.List;

/**
 * A basic representation of an Entity.
 * 
 * @author RDENAUX
 *
 */
public class UIEntity implements Serializable {

	private static final long serialVersionUID = 3490645737153292348L;

	@Override
	public String toString() {
		return "UIEntity [url=" + url + ", label=" + label + ", depictions="
				+ depictions + ", types=" + types + "]";
	}

	/**
	 * The URI for this {@link UIEntity}. This field is mandatory.
	 */
	private String url;
	
	/**
	 * The preferred label for this {@link UIEntity} in some language.
	 * This field is mandatory.
	 */
	private String label;
	
	/**
	 * Urls with depictions of this UIEntity
	 */
	private List<String> depictions;
	
	/**
	 * URIs for the known types of this {@link UIEntity}.
	 */
	private List<String> types;

	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<String> getDepictions() {
		return depictions;
	}

	public void setDepictions(List<String> depictions) {
		this.depictions = depictions;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}
	
	
}
