package eu.xlime.bean;

import javax.persistence.Id;

/**
 * Represents an Event as defined by the <a href="http://eventregistry.com">Event Registry</a>
 * and used within xLiMe.
 * 
 * @author RDENAUX
 *
 */
public class EREvent implements XLiMeResource {

	@Id
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
