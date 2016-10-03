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
	
	private String asrEngine;
	
	private String lang;

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

	public String getAsrEngine() {
		return asrEngine;
	}

	public void setAsrEngine(String asrEngine) {
		this.asrEngine = asrEngine;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	@Override
	public String toString() {
		return String
				.format("ASRAnnotation [url=%s, inSegment=%s, recognizedText=%s, asrEngine=%s, lang=%s]",
						url, inSegment, recognizedText, asrEngine, lang);
	}

}
