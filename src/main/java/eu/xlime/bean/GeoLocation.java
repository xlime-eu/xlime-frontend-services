package eu.xlime.bean;

import java.io.Serializable;

public class GeoLocation implements Serializable {

	private float lon;
	private float lat;
	private String label;
	public float getLon() {
		return lon;
	}
	public void setLon(float lon) {
		this.lon = lon;
	}
	public float getLat() {
		return lat;
	}
	public void setLat(float lat) {
		this.lat = lat;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
