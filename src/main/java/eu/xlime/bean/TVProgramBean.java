package eu.xlime.bean;

public class TVProgramBean extends MediaItem {

	private static final long serialVersionUID = -6229457491463595552L;

	private String url;
	
	private String title;
	
	private UIDate broadcastDate;
	
	private Content description;
	
	private Duration duration;
	
	private UrlLabel source;
	
	private String relatedImage;
	
	private UrlLabel publisher;
	
	private GeoLocation relatedLocation;
	
	public final String type = "http://www.w3.org/ns/ma-ont#MediaResource";

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public UIDate getBroadcastDate() {
		return broadcastDate;
	}

	public void setBroadcastDate(UIDate broadcastDate) {
		this.broadcastDate = broadcastDate;
	}

	public Content getDescription() {
		return description;
	}

	public void setDescription(Content description) {
		this.description = description;
	}


	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public UrlLabel getSource() {
		return source;
	}

	public void setSource(UrlLabel source) {
		this.source = source;
	}

	public String getRelatedImage() {
		return relatedImage;
	}

	public void setRelatedImage(String relatedImage) {
		this.relatedImage = relatedImage;
	}

	public UrlLabel getPublisher() {
		return publisher;
	}

	public void setPublisher(UrlLabel publisher) {
		this.publisher = publisher;
	}

	public GeoLocation getRelatedLocation() {
		return relatedLocation;
	}

	public void setRelatedLocation(GeoLocation relatedLocation) {
		this.relatedLocation = relatedLocation;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "TVProgramBean [url=" + url + ", title=" + title
				+ ", broadcastDate=" + broadcastDate + ", description="
				+ description + ", duration=" + duration + ", source=" + source
				+ ", relatedImage=" + relatedImage + ", publisher=" + publisher
				+ ", relatedLocation=" + relatedLocation + ", type=" + type
				+ "]";
	}

	
}
