package eu.xlime.bean;

import java.io.Serializable;

import eu.xlime.summa.bean.UIEntity;

public class EntityAnnotation implements Serializable {

	private static final long serialVersionUID = -368872994505907737L;
	
	private UIEntity entity;
	private double confidence;
	private String type = "http://xlime-project.org/vocab/EntityAnnotation";
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public UIEntity getEntity() {
		return entity;
	}
	public void setEntity(UIEntity entity) {
		this.entity = entity;
	}
	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	@Override
	public String toString() {
		return "EntityAnnotation [entity=" + entity + ", confidence="
				+ confidence + "]";
	}
	
}
