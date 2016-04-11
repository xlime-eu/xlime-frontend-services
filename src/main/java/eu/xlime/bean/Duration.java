package eu.xlime.bean;

import java.io.Serializable;

public class Duration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1879112502093156036L;
	private double totalSeconds;
	private int hours;
	private int minutes;
	private int seconds;
	
	public Duration(double totalSeconds) {
		super();
		this.totalSeconds = totalSeconds;
		hours = hoursFromTotalSeconds();
		minutes = minsFromTotalSeconds();
		seconds = secsFromTotalSeconds();
	}

	private int secsFromTotalSeconds() {
		int totSecs = (int)totalSeconds;
		return totSecs - (hoursFromTotalSeconds() * 60 * 60) - (minsFromTotalSeconds() * 60);
	}

	private int minsFromTotalSeconds() {
		int totalMins = (int) Math.floor(totalSeconds / 60); 
		return totalMins - (hoursFromTotalSeconds() * 60);
	}

	private int hoursFromTotalSeconds() {
		return (int)Math.floor(totalSeconds / 60 / 60);
	}

	public double getTotalSeconds() {
		return totalSeconds;
	}

	public void setTotalSeconds(double totalSeconds) {
		this.totalSeconds = totalSeconds;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	@Override
	public String toString() {
		return "Duration [totalSeconds=" + totalSeconds + ", hours=" + hours
				+ ", minutes=" + minutes + ", seconds=" + seconds + "]";
	}
	
}
