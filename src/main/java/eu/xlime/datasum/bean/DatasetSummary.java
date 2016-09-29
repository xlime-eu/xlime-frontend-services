package eu.xlime.datasum.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.xlime.bean.UIDate;


/**
 * Summarises an xLime Dataset by providing various metrics.
 * 
 * @author Nuria Garcia
 * {@link}: ngarcia@expertsystem.com
 *
 */

//@JsonTypeName(value="xlime-summarise")
@XmlRootElement(name="xlime-summarise")
public class DatasetSummary implements Serializable {
	
	/**
	 * A short name for the dataset being summarised
	 */
	private String name;
	/**
	 * A longer description about the dataset being summarised
	 */
	private String description;
	
	/**
	 * The date in which this {@link DatasetSummary} was generated
	 */
	private Date summaryDate;
	
	/**
	 * Provides a list of (human readable) errors which occurred when generating this {@link DatasetSummary} 
	 */
	private List<String> errors;
	/**
	 * Provides a list of (human readable) messages about the generation of this {@link DatasetSummary}.
	 */
	private List<String> messages;
	
	/**
	 * The number of activities in the dataset
	 */
	private Long activities;
	/**
	 * The number of microposts in the dataset
	 */
	private Long microposts;
	
	/**
	 * The date of the oldest micropost in this dataset 
	 */
	private UIDate oldestMicropostDate;
	
	/**
	 * The date of the newest microposts in this dataset
	 */
	private UIDate newestMicropostDate;
	
	/**
	 * A histogram with the number of microposts in the dataset, sorted by 
	 * day.
	 */
	private List<HistogramItem> micropostsByDay;
	
	/**
	 * The number of microposts in the dataset associated to some keyword-filters (used to filter the microposts from
	 * the source streams like Twitter, Facebook, etc.).
	 */
	private List<HistogramItem> microposts_filter;
	/**
	 * The number of news articles in the dataset
	 */
	private Long newsarticles;

	/**
	 * The date of the oldest news article in this dataset 
	 */
	private UIDate oldestNewsarticleDate;
	
	/**
	 * The date of the newest news article in this dataset
	 */
	private UIDate newestNewsarticleDate;
	
	/**
	 * The number of media resources (i.e. tv-programs) in the dataset
	 */
	private Long mediaresources;

	/**
	 * The date of the oldest mediaresource in this dataset 
	 */
	private UIDate oldestMediaresourceDate;
	
	/**
	 * The date of the newest mediaresource in this dataset
	 */
	private UIDate newestMediaresourceDate;
	
	/**
	 * The number of RDF triples required to represent the data (when the dataset is in RDF)
	 */
	private Long triples;
	/**
	 * The number of RDF entities in the dataset (when the dataset is in RDF) 
	 */
	private Long entities;
	/**
	 * The number of distinct RDF subjects in the dataset (when the dataset is in RDF)
	 */
	private Long subjects;
	/**
	 * The number of distinct RDF predicates (when the dataset is in RDF) 
	 */
	private Long predicates;
	/**
	 * The number of distinct RDF objects (when the dataset is in RDF)
	 */
	private Long objects;
	
	/**
	 * The number of distinct RDF instances per RDF/OWL class (when the dataset is in RDF)
	 */
	private List<HistogramItem> instancesPerClass;
	/**
	 * The number of distinct RDF triples per property/predicate (when the dataset is in RDF)
	 */
	private List<HistogramItem> tripesPerProperty;
		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getSummaryDate() {
		return summaryDate;
	}
	public void setSummaryDate(Date summaryDate) {
		this.summaryDate = summaryDate;
	}
	
	public List<String> getErrors() {
		return errors;
	}
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	public Long getActivities() {
		return activities;
	}
	public void setActivities(Long activities) {
		this.activities = activities;
	}
	public Long getMicroposts() {
		return microposts;
	}
	public void setMicroposts(Long microposts) {
		this.microposts = microposts;
	}	
	
	public List<HistogramItem> getMicropostsByDay() {
		return micropostsByDay;
	}
	public void setMicropostsByDay(List<HistogramItem> micropostsByDay) {
		this.micropostsByDay = micropostsByDay;
	}
	public UIDate getOldestMicropostDate() {
		return oldestMicropostDate;
	}
	public void setOldestMicropostDate(UIDate oldestMicropostDate) {
		this.oldestMicropostDate = oldestMicropostDate;
	}
	public UIDate getNewestMicropostDate() {
		return newestMicropostDate;
	}
	public void setNewestMicropostDate(UIDate newestMicropostDate) {
		this.newestMicropostDate = newestMicropostDate;
	}
	public List<HistogramItem> getMicroposts_filter() {
		return microposts_filter;
	}
	public void setMicroposts_filter(List<HistogramItem> microposts_filter) {
		this.microposts_filter = microposts_filter;
	}
	public Long getNewsarticles() {
		return newsarticles;
	}
	public void setNewsarticles(Long newsarticles) {
		this.newsarticles = newsarticles;
	}
	
	public UIDate getOldestNewsarticleDate() {
		return oldestNewsarticleDate;
	}
	public void setOldestNewsarticleDate(UIDate oldestNewsarticleDate) {
		this.oldestNewsarticleDate = oldestNewsarticleDate;
	}
	public UIDate getNewestNewsarticleDate() {
		return newestNewsarticleDate;
	}
	public void setNewestNewsarticleDate(UIDate newestNewsarticleDate) {
		this.newestNewsarticleDate = newestNewsarticleDate;
	}
	public Long getMediaresources() {
		return mediaresources;
	}
	public void setMediaresources(Long mediaresources) {
		this.mediaresources = mediaresources;
	}

	public UIDate getOldestMediaresourceDate() {
		return oldestMediaresourceDate;
	}
	public void setOldestMediaresourceDate(UIDate oldestMediaresourceDate) {
		this.oldestMediaresourceDate = oldestMediaresourceDate;
	}
	public UIDate getNewestMediaresourceDate() {
		return newestMediaresourceDate;
	}
	public void setNewestMediaresourceDate(UIDate newestMediaresourceDate) {
		this.newestMediaresourceDate = newestMediaresourceDate;
	}
	public Long getTriples() {
		return triples;
	}
	public void setTriples(Long triples) {
		this.triples = triples;
	}
	
	public Long getEntities() {
		return entities;
	}
	public void setEntities(Long entities) {
		this.entities = entities;
	}
	public Long getSubjects() {
		return subjects;
	}
	public void setSubjects(Long subjects) {
		this.subjects = subjects;
	}
	public Long getPredicates() {
		return predicates;
	}
	public void setPredicates(Long predicates) {
		this.predicates = predicates;
	}
	public Long getObjects() {
		return objects;
	}
	public void setObjects(Long objects) {
		this.objects = objects;
	}	
	public List<HistogramItem> getInstancesPerClass() {
		return instancesPerClass;
	}
	public void setInstancesPerClass(List<HistogramItem> instancesPerClass) {
		this.instancesPerClass = instancesPerClass;
	}
	public List<HistogramItem> getTripesPerProperty() {
		return tripesPerProperty;
	}
	public void setTripesPerProperty(List<HistogramItem> tripesPerProperty) {
		this.tripesPerProperty = tripesPerProperty;
	}
	
	@Override
	public String toString() {
		return String
				.format("DatasetSummary [name=%s, description=%s, summaryDate=%s, errors=%s, messages=%s, activities=%s, microposts=%s, oldestMicropostDate=%s, newestMicropostDate=%s, micropostsByDay=%s, microposts_filter=%s, newsarticles=%s, oldestNewsarticleDate=%s, newestNewsarticleDate=%s, mediaresources=%s, oldestMediaresourceDate=%s, newestMediaresourceDate=%s, triples=%s, entities=%s, subjects=%s, predicates=%s, objects=%s, instancesPerClass=%s, tripesPerProperty=%s]",
						name, description, summaryDate, errors, messages,
						activities, microposts, oldestMicropostDate,
						newestMicropostDate, micropostsByDay,
						microposts_filter, newsarticles, oldestNewsarticleDate,
						newestNewsarticleDate, mediaresources,
						oldestMediaresourceDate, newestMediaresourceDate,
						triples, entities, subjects, predicates, objects,
						instancesPerClass, tripesPerProperty);
	}
}
