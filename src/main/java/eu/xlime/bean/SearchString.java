package eu.xlime.bean;

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

	private static final long serialVersionUID = 7817062006086569275L;
	
	/**
	 * The value of the Search String
	 */
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "SearchString [value=" + value + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
