package eu.xlime.bean;

import java.io.Serializable;

/**
 * Pair of URL and label. This is useful for providing a human-readable label
 * to a particular URL.
 * 
 * @author RDENAUX
 *
 */
public class UrlLabel implements Serializable {

	/**
	 * The URL value
	 */
	private String url;
	
	/**
	 * The human-readable label to associate with {@link #url}.
	 */
	private String label;

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
	
	@Override
	public String toString() {
		return "UrlLabel [url=" + url + ", label=" + label + "]";
	}
	
}
