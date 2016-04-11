package eu.xlime.bean;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author RDENAUX
 * @deprecated use {@link MediaItemListBean} instead
 */
@XmlRootElement(name = "mediaItem")
public class SingleMediaItemBean {
	private MediaItem mediaItem;

	public MediaItem getMediaItem() {
		return mediaItem;
	}

	public void setMediaItem(MediaItem mediaItem) {
		this.mediaItem = mediaItem;
	}

	@Override
	public String toString() {
		return "SingleMediaItemBean.of(" + mediaItem + ")";
	}
	
	
}
