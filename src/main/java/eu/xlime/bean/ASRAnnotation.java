package eu.xlime.bean;

/**
 * Represents the output of some ASR process on an audio stream.
 * 
 * @author RDENAUX
 *
 */
public class ASRAnnotation implements XLiMeResource {

	private static final long serialVersionUID = -492673668791572135L;

	private VideoSegment inSegment;
	
	private String recognizedText;

	public VideoSegment getInSegment() {
		return inSegment;
	}

	public void setInSegment(VideoSegment inSegment) {
		this.inSegment = inSegment;
	}

	public String getRecognizedText() {
		return recognizedText;
	}

	public void setRecognizedText(String recognizedText) {
		this.recognizedText = recognizedText;
	}
	
}
