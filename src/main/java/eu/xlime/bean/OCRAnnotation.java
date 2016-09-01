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
	/**
	 * The Url of the activity that recognised the {@link #entity} in the annotated 
	 * {@link #resourceUrl}. Optional, but when available it is useful for looking 
	 * up the context in which the annotation was created. 
	 */
	private String activityUrl;
	
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
	
	public String getActivityUrl() {
		return activityUrl;
	}
	public void setActivityUrl(String activityUrl) {
		this.activityUrl = activityUrl;
	}
	@Override
	public String toString() {
		return String
				.format("OCRAnnotation [url=%s, inSegment=%s, recognizedText=%s, activityUrl=%s]",
						url, inSegment, recognizedText, activityUrl);
	}
}
