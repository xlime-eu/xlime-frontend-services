package eu.xlime.bean;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mediaItem")
public class SingleMediaItemBean {
	private MediaItem mediaItem;

	public MediaItem getMediaItem() {
		return mediaItem;
	}

	public void setMediaItem(MediaItem mediaItem) {
		this.mediaItem = mediaItem;
	}
}
