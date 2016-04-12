package eu.xlime.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mediaItems")
public class MediaItemListBean {
	
	private List<MediaItem> mediaItems = new ArrayList<>();
	
	private List<String> errors = new ArrayList<>();

	public List<MediaItem> getMediaItems() {
		return mediaItems;
	}

	public void addMediaItem(MediaItem mediaItem) {
		this.mediaItems.add(mediaItem);
	}
	
	public void addError(String message) {
		errors.add(message);
	}
	
	public void setMediaItems(List<MediaItem> mediaItems) {
		this.mediaItems = mediaItems;
	}

	public List<String> getErrors() {
		return errors;
	}

	@Override
	public String toString() {
		return "MediaItemListBean [mediaItems=" + mediaItems + "]";
	}
	
}
