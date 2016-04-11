package eu.xlime.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UIDate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8807252979702149326L;

	private static final Logger log = LoggerFactory.getLogger(UIDate.class);
	
	public Date timestamp;
	public String formatted;
	public String timeAgo;
	
	public static final String defaultDatePattern = "dd/MM/yyyy hh:mm:ss";

	private UIDate() {
	}
	
	public Date getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


	public String getFormatted() {
		return formatted;
	}


	public void setFormatted(String formatted) {
		this.formatted = formatted;
	}


	public String getTimeAgo() {
		return timeAgo;
	}


	public void setTimeAgo(String timeAgo) {
		this.timeAgo = timeAgo;
	}


	public UIDate(Date aDate) {
		this(aDate, defaultDatePattern);
	}
	
	public UIDate(Date aDate, String dateFormat) {
		timestamp = aDate;
		formatted = safeFormat(aDate, dateFormat); 
		timeAgo = calcTimeAgo(aDate);
	}

	private String safeFormat(Date aDate, String datePattern) {
		try {
			return new SimpleDateFormat(datePattern).format(aDate);
		} catch (Exception e) {
			log.warn("Error formatting date", e);
			return new SimpleDateFormat(defaultDatePattern).format(aDate); 
		}
	}

	private String calcTimeAgo(Date past) {
		Date now = new Date();
        long daysAgo = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
        if (daysAgo > 0) return String.format("%s %s ago", daysAgo, "days");
        
        long hoursAgo = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
        if (hoursAgo > 0) return String.format("%s %s ago", hoursAgo, "hours");

        long minsAgo = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
        if (minsAgo > 0) return String.format("%s %s ago", minsAgo, "minutes");
        
        long secsAgo = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
        if (secsAgo > 0) return String.format("%s %s ago", secsAgo, "seconds");
        
        if (secsAgo < 0) return String.format("in %ss?", secsAgo);
        
		return "now";
	}
}
