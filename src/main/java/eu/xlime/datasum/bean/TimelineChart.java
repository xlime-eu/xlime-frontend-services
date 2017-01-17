package eu.xlime.datasum.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.xlime.bean.XLiMeResource;

/**
 * Represents a timeline chart consisting of one or more numeric values
 * over a specific timeline of {@link #dates}.
 * 
 * @author rdenaux
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TimelineChart implements XLiMeResource {

	private static final long serialVersionUID = -3531922257314741415L;

	@Id
	private String url;
	
	private Date date;
	
	private List<Date> dates;
	
	private List<String> labels;
	
	private List<Dataset> datasets;
	
	@Id
	public final String getUrl() {
		return url;
	}


	public final void setUrl(String url) {
		this.url = url;
	}


	public final Date getDate() {
		return date;
	}


	public final void setDate(Date date) {
		this.date = date;
	}


	public final List<Date> getDates() {
		return dates;
	}


	public final void setDates(List<Date> dates) {
		this.dates = dates;
	}


	public final List<String> getLabels() {
		return labels;
	}


	public final void setLabels(List<String> labels) {
		this.labels = labels;
	}


	public final List<Dataset> getDatasets() {
		return datasets;
	}


	public final void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	@Override
	public String toString() {
		return String
				.format("TimelineChart [url=%s, date=%s, dates=%s, labels=%s, datasets=%s]",
						url, date, dates, labels, datasets);
	}

	@JsonTypeInfo(  
		    use = JsonTypeInfo.Id.NAME,  
		    include = JsonTypeInfo.As.PROPERTY,  
		    property = "@type")  
	@JsonSubTypes({  
		    @Type(value = LongDataset.class, name = "http://xlime.eu/vocab/TimelineChart/LongDataset"),  
		    @Type(value = DoubleDataset.class, name = "http://xlime.eu/vocab/TimelineChart/DoubleDataset")
		    }) 
	public static interface Dataset extends Serializable {
		
	}

	public static abstract class BaseDatasetImpl<T> implements Dataset {
		private String label;
		private List<T> data;
		public final String getLabel() {
			return label;
		}
		public final void setLabel(String label) {
			this.label = label;
		}
		public final List<T> getData() {
			return data;
		}
		public final void setData(List<T> data) {
			this.data = data;
		}
	}
	
	public static class LongDataset extends BaseDatasetImpl<Long> {
		private static final long serialVersionUID = -7977798273010051009L;

		@Override
		public String toString() {
			return String.format("LongDataset [getLabel()=%s, getData()=%s]",
					getLabel(), getData());
		}
	}
	
	public static class DoubleDataset extends BaseDatasetImpl<Double> {

		private static final long serialVersionUID = -1613067609291417347L;
		
		@Override
		public String toString() {
			return String.format("DoubleDataset [getLabel()=%s, getData()=%s]",
					getLabel(), getData());
		}
		
	}
}
