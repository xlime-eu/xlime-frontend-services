package eu.xlime.bean;

/**
 * Represents the output of some OCR annotation on a given video segment.
 * @author RDENAUX
 *
 */
public class OCRAnnotation implements XLiMeResource {

	private static final long serialVersionUID = 4212794730125578348L;
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
