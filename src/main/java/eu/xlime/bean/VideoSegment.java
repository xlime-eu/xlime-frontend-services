package eu.xlime.bean;

import javax.persistence.Id;

/**
 * Represents a segment of a full video. For example, a few seconds or minutes of a TV programme.
 *  
 * @author RDENAUX
 *
 */
public class VideoSegment implements XLiMeResource {
	
	private static final long serialVersionUID = 6318591964012556911L;

	@Id
	private String url;
	
	/**
	 * Link to the original {@link TVProgramBean}
	 */
	private TVProgramBean partOf;

	/**
	 * Optional startTime (broadcast date) of this {@link VideoSegment}. 
	 */
	private UIDate startTime;
	
	/**
	 * Information about where in the {@link TVProgramBean}, which this {@link VideoSegment} is a {@link #partOf}, 
	 * this {@link VideoSegment} is located.
	 */
	private VideoSegmentPosition position;
	
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public TVProgramBean getPartOf() {
		return partOf;
	}

	public void setPartOf(TVProgramBean partOf) {
		this.partOf = partOf;
	}

	public UIDate getStartTime() {
		return startTime;
	}

	public void setStartTime(UIDate startTime) {
		this.startTime = startTime;
	}

	public VideoSegmentPosition getPosition() {
		return position;
	}

	public void setPosition(VideoSegmentPosition position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return String.format(
				"VideoSegment [url=%s, partOf=%s, startTime=%s, position=%s]",
				url, partOf, startTime, position);
	}


}
