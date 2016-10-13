package eu.xlime.bean;

import java.util.ArrayList;
import java.util.List;

import eu.xlime.summa.bean.UIEntity;

public class SearchResultBean {

	private List<MediaItem> mediaItems = new ArrayList<>();

	private List<UIEntity> entities = new ArrayList<>();
	
	private List<XLiMeResource> annotations = new ArrayList<>();
	
	private List<String> errors = new ArrayList<>();

	public void addMediaItem(MediaItem mediaItem) {
		this.mediaItems.add(mediaItem);
	}
	
	public final List<XLiMeResource> getAnnotations() {
		return annotations;
	}

	public void addError(String message) {
		errors.add(message);
	}
	
	public List<MediaItem> getMediaItems() {
		return mediaItems;
	}

	public void setMediaItems(List<MediaItem> mediaItems) {
		this.mediaItems = mediaItems;
	}

	public List<UIEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<UIEntity> entities) {
		this.entities = entities;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		return "SearchResultBean [mediaItems=" + mediaItems + ", entities="
				+ entities + ", errors=" + errors + "]";
	}
		
}
