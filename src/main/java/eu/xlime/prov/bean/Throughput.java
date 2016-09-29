package eu.xlime.prov.bean;

/**
 * Utility bean to capture the throughput of some computational activity.
 * 
 * @author rdenaux
 *
 */
public class Throughput {

	private double value;
	/**
	 * For example: "kb/s" or "items/s"
	 */
	private String unit;
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	@Override
	public String toString() {
		return String.format("Throughput [value=%s, unit=%s]", value, unit);
	}
}
