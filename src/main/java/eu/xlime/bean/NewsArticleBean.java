package eu.xlime.bean;


//import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

//@XmlDiscriminatorValue("http://kdo.render-project.eu/kdo#NewsArticle")
public class NewsArticleBean implements MediaItem {

	private static final long serialVersionUID = -6418249353449068589L;

	private String url;
	
	private Content content;
	
	private String title;
	
	private UIDate created;
	
	private String lang;
	
	private UrlLabel publisher;
	
	private String source;
	
	private GeoLocation location;
	
	public final String type = "http://kdo.render-project.eu/kdo#NewsArticle";

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public GeoLocation getLocation() {
		return location;
	}

	public void setLocation(GeoLocation location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "NewsArticleBean [url=" + url + ", content=" + content
				+ ", title=" + title + ", created=" + created + ", lang="
				+ lang + ", publisher=" + publisher + ", source=" + source
				+ ", location=" + location + "]";
	}
	
}
