package eu.xlime.bean;

import javax.persistence.Id;

/**
 * Represents the output of some ASR process on an audio stream.
 * 
 * @author RDENAUX
 *
 */
public class ASRAnnotation implements XLiMeResource {

	private static final long serialVersionUID = -3665559261491087237L;

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

	@Override
	public String toString() {
		return String.format(
				"ASRAnnotation [url=%s, inSegment=%s, recognizedText=%s]", url,
				inSegment, recognizedText);
	}

}
