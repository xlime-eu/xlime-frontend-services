package eu.xlime.bean;

import java.util.Date;

public class MicroPostBean extends MediaItem {

	private String url;
	
	private Content content;
	
	private Date created;
	
	private String lang;
	
	private UrlLabel publisher;
	
	private String source;
	
	private String sourceType;
	
	private UrlLabel creator;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public UrlLabel getPublisher() {
		return publisher;
	}

	public void setPublisher(UrlLabel publisher) {
		this.publisher = publisher;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public UrlLabel getCreator() {
		return creator;
	}

	public void setCreator(UrlLabel creator) {
		this.creator = creator;
	}	
	
}
