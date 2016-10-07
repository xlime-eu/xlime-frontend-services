package eu.xlime.bean;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Generic class for reporting on various metrics related to the xLiMe platform.
 * 
 * Essentially, this class allows us to have various <b>meters</b> collecting and reporting at specific <b>times</b> 
 * on various <b>metrics</b>.
 * 
 * @author rdenaux
 * @author osalas
 */
public class StatMetrics implements XLiMeResource {
	
	private static final long serialVersionUID = -3027143090223202247L;
	private static final Logger log = LoggerFactory.getLogger(StatMetrics.class);

	/**
	 * Default algorithm for naming a {@link StatMetrics} instance based on its {@link #meterId} and creation {@link #date}. 
	 * @param inst the instance for which you want to coin a uri
	 * @return
	 */
	public static String coinUri(StatMetrics inst) {
		return String.format("http://xlime.eu/vocab/statMetrics?meterId=%s&timestamp=%s", inst.meterId, inst.date.getTime());
	}
	
	/**
	 * An identifier for this {@link StatMetrics}.
	 */
	@Id
	private String url;
	
	/**
	 * An id of the agent that produced this {@link StatMetrics}
	 */
	private String meterId;
	
	/**
	 * The date that the {@link #meterId} started collecting data.
	 */
	private Date meterStartDate;
	
	/**
	 * Zero or more counters, which be aggregated to produce a time series
	 */
	private Map<String,Long> counters;
	
	/**
	 * The date when this {@link StatMetrics} were produced by the {@link #meterId}.
	 * This ought to be after the {@link #meterStartDate}. 
	 */
	private Date date; 
	
	public StatMetrics(){
		this.date = Calendar.getInstance().getTime();
		this.counters = new HashMap<String,Long>();
	}

	/**
	 * Use this method to calculate a url based on the {@link #meterId} and the {@link #date}.
	 */
	public void setUrl() {
		url = StatMetrics.coinUri(this);
	}
	
	public String getUrl() {
		if (url == null) setUrl();
		return url;
	}

	public String getMeterId() {
		return this.meterId;
	}

	public void setMeterId(String aMeterId) {
		this.meterId = aMeterId;
	}

	public final Date getMeterStartDate() {
		return meterStartDate;
	}

	public final void setMeterStartDate(Date meterStartDate) {
		this.meterStartDate = meterStartDate;
	}

	public Map<String,Long> getCounters(){
		return this.counters;
	}
	
	public void setCounter(String name, Long count){
		if (counters.containsKey(name)) {
			log.debug(String.format("Overwriting count for %s (%s -> %s)", name, counters.get(name), count));
		}
		this.counters.put(name, count);
	}
	
	public void addCounters(Map<String,Long> newCounters){
		for (String key: newCounters.keySet()){
			if (counters.containsKey(key)) {
				log.debug(String.format("Overwriting count for %s (%s -> %s)", key, counters.get(key), newCounters.get(key)));
			}
			this.counters.put(key, newCounters.get(key));
		}
	}

	public Date getDate(){
		return this.date;
	}

	@Override
	public String toString() {
		return String.format(
				"StatMetrics [url=%s, meterId=%s, date=%s, counters=%s]", url,
				meterId, date, counters);
	}

}
