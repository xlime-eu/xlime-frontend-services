package eu.xlime.summa.bean;

import java.util.List;

import javax.persistence.Id;

import eu.xlime.bean.XLiMeResource;

/**
 * A basic representation of an Entity.
 * 
 * @author RDENAUX
 *
 */
public class UIEntity implements XLiMeResource {

	private static final long serialVersionUID = 3490645737153292348L;

	@Override
	public String toString() {
		return "UIEntity [url=" + url + ", label=" + label + ", depictions="
				+ depictions + ", types=" + types + "]";
	}

	@Id
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UIEntity other = (UIEntity) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
}
