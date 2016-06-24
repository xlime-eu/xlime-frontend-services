package eu.xlime.bean;

import javax.persistence.Id;

/**
 * Represents a single search {@link String} input by some user. 
 * The search string may contain multiple tokens and may be in a
 * variety of languages. Examples:
 * <ul>
 * <li><code>puma roma basic</code></li>
 * <li><code>neue Ansatz-Kaschiers</code></li>
 * </ul>
 * 
 * @author RDENAUX
 *
 */
public class SearchString implements XLiMeResource {

	private static final long serialVersionUID = -7569466264514317915L;

	@Id
	private String url;
	
	/**
	 * The value of the Search String
	 */
	private String value;

	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("SearchString [url=%s, value=%s]", url, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		SearchString other = (SearchString) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
