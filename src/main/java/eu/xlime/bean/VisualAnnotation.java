package eu.xlime.bean;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the output of some visual object recognition annotation
 * 
 * @author rdenaux
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class VisualAnnotation implements XLiMeResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3832199248878889509L;

	@Id
	private String url;
	
	private VideoSegment inSegment;
	
	private double confidence;
	
	/**
	 * URI identifying the object recognized
	 */
	private String recognizedObject;
	
	/**
	 * Name or uri for the engine that performed the visual object recognition.  
	 */
	private String recognizerEngine;
	
	@Override
	public String getUrl() {
		return url;
	}

	public final VideoSegment getInSegment() {
		return inSegment;
	}

	public final void setInSegment(VideoSegment inSegment) {
		this.inSegment = inSegment;
	}

	public final String getRecognizedObject() {
		return recognizedObject;
	}

	public final void setRecognizedObject(String recognizedObject) {
		this.recognizedObject = recognizedObject;
	}

	
	public final double getConfidence() {
		return confidence;
	}

	public final void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public final String getRecognizerEngine() {
		return recognizerEngine;
	}

	public final void setRecognizerEngine(String recognizerEngine) {
		this.recognizerEngine = recognizerEngine;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return String
				.format("VisualAnnotation [url=%s, confidence=%s, recognizedObject=%s, recognizerEngine=%s, inSegment=%s]",
						url, confidence, recognizedObject, recognizerEngine,
						inSegment);
	}

	
}
