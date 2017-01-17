package eu.xlime.bean;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import javax.persistence.Id;

import eu.xlime.bean.annpos.AnnotationPosition;
import eu.xlime.bean.annpos.SpanInTextPosition;
import eu.xlime.summa.bean.UIEntity;

/**
 * Provides information about the 
 * @author rdenaux
 *
 */
public class EntityAnnotation implements XLiMeResource, Serializable {

	private static final long serialVersionUID = -368872994505907737L;
	
	public static String coinUri(EntityAnnotation ea) {
		if (ea == null) return "http://xlime.eu/vocab/EntityAnnotation/null";
		final String posParams = positionAsParams(ea.getPosition());
		return 
				String.format("http://xlime-project.org/vocab/EntityAnnotation?kbentity=%s&resource=%s&activity=%s%s", 
						enc(ea.getEntity() == null ? "null" : ea.getEntity().getUrl()),
						enc(ea.getResourceUrl()),
						enc(ea.getActivityUrl()),
						(posParams == null || posParams.equals("")) ? "" : "&" + posParams);
	}
		
	private static String positionAsParams(AnnotationPosition position) {
		if (position == null) return "";
		if (position instanceof SpanInTextPosition) {
			SpanInTextPosition spanPos = (SpanInTextPosition) position;
			return String.format("text-start=%s&text-end=%s", enc("" + spanPos.getStart()), enc("" + spanPos.getEnd()));
		} else return "";
	}

	private static String enc(String string) {
		if (string == null) return null;
		try {
			return URLEncoder.encode(string, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * The entity that was recognised in the annotated {@link #resourceUrl}
	 */
	private UIEntity entity;
	/**
	 * The Url of the activity that recognised the {@link #entity} in the annotated 
	 * {@link #resourceUrl}. Optional, but when available it is useful for looking 
	 * up the context in which the annotation was created. 
	 */
	private String activityUrl;
	/**
	 * Url of an xLiMe resource that was analysed in order to find a mention to 
	 * the {@link #entity}.
	 */
	private String resourceUrl;
	/**
	 * The confidence value given by the {@link #activityUrl} to the recognition
	 * of the {@link #entity}. E.g. this could be low if the mention is highly 
	 * ambiguous, or if the annotated {@link #resourceUrl} does not provide enough 
	 * context to provide a good disambiguation.  
	 */
	private double confidence;
	/**
	 * Optional information about the position of the recognised {@link #entity} in the
	 * annotated resource (given by {@link #resourceUrl}).
	 */
	private AnnotationPosition position;
	
	/**
	 * Date in which this {@link EntityAnnotation} was created. 
	 * This should be a date (slightly) after the creation/publishing date of the
	 * resource (identified by {@link #resourceUrl}). 
	 */
	private Date insertionDate;
	
	/**
	 * Date when the resource being annotated was published. For example, if this annotates 
	 * an ASR segment, this should be the broadcast date of the segment.
	 */
	private UIDate mentionDate;
	
	/**
	 * A url for identifying this {@link EntityAnnotation}
	 */
	@Id
	private String url;
	
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
	
	public String getActivityUrl() {
		return activityUrl;
	}
	public void setActivityUrl(String activityUrl) {
		this.activityUrl = activityUrl;
	}
	public String getResourceUrl() {
		return resourceUrl;
	}
	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}
	public AnnotationPosition getPosition() {
		return position;
	}
	public void setPosition(AnnotationPosition position) {
		this.position = position;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public final Date getInsertionDate() {
		return insertionDate;
	}

	public final void setInsertionDate(Date insertionDate) {
		this.insertionDate = insertionDate;
	}

	public final UIDate getMentionDate() {
		return mentionDate;
	}

	public final void setMentionDate(UIDate mentionDate) {
		this.mentionDate = mentionDate;
	}

	@Override
	public String toString() {
		return String
				.format("EntityAnnotation [entity=%s, activityUrl=%s, resourceUrl=%s, confidence=%s, position=%s, insertionDate=%s, mentionDate=%s, url=%s]",
						entity, activityUrl, resourceUrl, confidence, position,
						insertionDate, mentionDate, url);
	}

	
}
