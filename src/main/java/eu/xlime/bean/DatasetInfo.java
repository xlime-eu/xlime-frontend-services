package eu.xlime.bean;

import javax.persistence.Id;

public class DatasetInfo implements XLiMeResource {

	private static final long serialVersionUID = 3998777441415312092L;
	
	@Id
	private String url;
	private String name;
	private String description;
	
	@Override
	public String getUrl() {
		return url;
	}

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return description;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return String.format("DatasetInfo [url=%s, name=%s, description=%s]",
				url, name, description);
	}

}
