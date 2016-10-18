package eu.xlime.datasum.bean;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.xlime.bean.UIDate;
import eu.xlime.bean.XLiMeResource;

/**
 * Part of a {@link DatasetSummary} containing counts and information about a particular type of 
 * {@link XLiMeResource}.
 * 
 * @author rdenaux
 *
 */
@XmlRootElement(name="xlime-resource-summary")
public class ResourceSummary implements Serializable {

	/**
	 * The number of {@link XLiMeResource}s in the dataset
	 */
	long count;
	
	/**
	 * The date of the oldest {@link XLiMeResource} in this dataset 
	 */
	UIDate oldestDate;
	
	/**
	 * The date of the newest {@link XLiMeResource} in this dataset 
	 */
	UIDate newestDate;
	
	/**
	 * A histogram with the number of {@link XLiMeResource}s in the dataset, sorted by 
	 * day, for some window of time (generally ranging between {@link #oldestDate} and {@link #newestDate}).
	 */
	List<HistogramItem> histogramPerDay;

	public final long getCount() {
		return count;
	}

	public final void setCount(long count) {
		this.count = count;
	}

	public final UIDate getOldestDate() {
		return oldestDate;
	}

	public final void setOldestDate(UIDate oldestDate) {
		this.oldestDate = oldestDate;
	}

	public final UIDate getNewestDate() {
		return newestDate;
	}

	public final void setNewestDate(UIDate newestDate) {
		this.newestDate = newestDate;
	}

	public final List<HistogramItem> getHistogramPerDay() {
		return histogramPerDay;
	}

	public final void setHistogramPerDay(List<HistogramItem> histogramPerDay) {
		this.histogramPerDay = histogramPerDay;
	}

	@Override
	public String toString() {
		return String
				.format("ResourceSummary [count=%s, oldestDate=%s, newestDate=%s, histogramPerDay=%s]",
						count, oldestDate, newestDate, histogramPerDay);
	}
	
	
}
