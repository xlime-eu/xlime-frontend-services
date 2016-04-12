package eu.xlime.bean;

/**
 * Represents a segment of a full video. For example, a few seconds or minutes of a TV programme.
 *  
 * @author RDENAUX
 *
 */
public class VideoSegment implements XLiMeResource {
	
	private TVProgramBean partOf;

	public TVProgramBean getPartOf() {
		return partOf;
	}

	public void setPartOf(TVProgramBean partOf) {
		this.partOf = partOf;
	}
	
	//TODO: what else can we include here? start and end time?
	
}
