package eu.xlime.bean;

/**
 * The contents of some subtitle stream that is part of a {@link VideoSegment}.
 * 
 * @author RDENAUX
 *
 */
public class SubtitleSegment implements XLiMeResource {

	private static final long serialVersionUID = -7386984941969124365L;

	private VideoSegment partOf;
	
	private String text;

	public VideoSegment getPartOf() {
		return partOf;
	}

	public void setPartOf(VideoSegment partOf) {
		this.partOf = partOf;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	
}
