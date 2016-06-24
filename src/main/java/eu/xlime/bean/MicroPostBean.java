package eu.xlime.bean;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown=true)
public class MicroPostBean implements MediaItem {

	private static final long serialVersionUID = -2888963022728761498L;

	@Id
	private String url;
	
	private Content content;
	
	private UIDate created;
	
	private String lang;
	
	private UrlLabel publisher;
	
	private String source;
	
	private String sourceType;
	
	private UrlLabel creator;
	
	public final String type = "http://rdfs.org/sioc/ns#MicroPost";
	
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

	public UIDate getCreated() {
		return created;
	}

	public void setCreated(UIDate created) {
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

	@Override
	public String toString() {
		return "MicroPostBean [url=" + url + ", content=" + content
				+ ", created=" + created + ", lang=" + lang + ", publisher="
				+ publisher + ", source=" + source + ", sourceType="
				+ sourceType + ", creator=" + creator + "]";
	}	

	
}
