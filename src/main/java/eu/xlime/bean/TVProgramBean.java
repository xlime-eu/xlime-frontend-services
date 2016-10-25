package eu.xlime.bean;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TVProgramBean implements MediaItem {

	private static final long serialVersionUID = -6229457491463595552L;

	@Id
	private String url;
	
	private String title;
	
	private UIDate broadcastDate;
	
	private Content description;
	
	private Duration duration;
	
	private UrlLabel source;
	
	private String relatedImage;
	
	private String genre;
	
	private UrlLabel publisher;
	
	private GeoLocation relatedLocation;
	
	private CustomTVInfo customInfo;
	
	/**
	 * Optional url where this TV program can be watched
	 */
	private String watchUrl;
	
	public final String type = "http://www.w3.org/ns/ma-ont#MediaResource";

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getWatchUrl() {
		return watchUrl;
	}

	public void setWatchUrl(String watchUrl) {
		this.watchUrl = watchUrl;
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

	public final String getGenre() {
		return genre;
	}

	public final void setGenre(String genre) {
		this.genre = genre;
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

	
	public final CustomTVInfo getCustomInfo() {
		return customInfo;
	}

	public final void setCustomInfo(CustomTVInfo customInfo) {
		this.customInfo = customInfo;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String
				.format("TVProgramBean [url=%s, title=%s, broadcastDate=%s, description=%s, duration=%s, source=%s, relatedImage=%s, genre=%s, publisher=%s, relatedLocation=%s, customInfo=%s, watchUrl=%s, type=%s]",
						url, title, broadcastDate, description, duration,
						source, relatedImage, genre, publisher,
						relatedLocation, customInfo, watchUrl, type);
	}

	
}
