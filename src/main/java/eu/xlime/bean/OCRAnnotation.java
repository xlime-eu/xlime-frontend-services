package eu.xlime.bean;

import javax.persistence.Id;

/**
 * Represents the output of some OCR annotation on a given video segment.
 * @author RDENAUX
 *
 */
public class OCRAnnotation implements XLiMeResource {

	private static final long serialVersionUID = 2519007514533234438L;
	
	@Id
	private String url;
	private VideoSegment inSegment;
	private String recognizedText;
	
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
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
