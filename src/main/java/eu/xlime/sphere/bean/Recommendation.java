package eu.xlime.sphere.bean;

import eu.xlime.bean.XLiMeResource;

/**
 * Recommends a particular {@link XLiMeResource} with a certain confidence value.
 * 
 * TODO: possibly expand this with explanations or with conflicts if they're required.
 * 
 * @author RDENAUX
 *
 */
public class Recommendation {

	private XLiMeResource recommended;
	
	private double confidence;

	public XLiMeResource getRecommended() {
		return recommended;
	}

	public void setRecommended(XLiMeResource recommended) {
		this.recommended = recommended;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	@Override
	public String toString() {
		return String.format("Recommendation [recommended=%s, confidence=%s]",
				recommended, confidence);
	}
	
}
