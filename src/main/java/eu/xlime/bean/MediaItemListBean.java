package eu.xlime.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mediaItems")
public class MediaItemListBean {
	
	private List<MediaItem> mediaItems = new ArrayList<>();
	
	private List<String> errors = new ArrayList<>();
	
	private List<String> messages = new ArrayList<>();

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

	
	public List<String> getMessages() {
		return messages;
	}
	
	public void addMessage(String message) {
		messages.add(message);
	}

	@Override
	public String toString() {
		final int maxLen = 5;
		return String.format(
				"MediaItemListBean [mediaItems=%s, errors=%s, messages=%s]",
				mediaItems != null ? mediaItems.subList(0,
						Math.min(mediaItems.size(), maxLen)) : null,
				errors != null ? errors.subList(0,
						Math.min(errors.size(), maxLen)) : null,
				messages != null ? messages.subList(0,
						Math.min(messages.size(), maxLen)) : null);
	}
}
