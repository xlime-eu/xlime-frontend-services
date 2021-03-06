package eu.xlime.bean;

public class ZattooStreamPosition implements VideoSegmentPosition {

	private static final long serialVersionUID = -8799747517026157434L;
	
	/**
	 * Streamposition value used by xLiMe for Zattoo streams.
	 */
	private long value;

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("ZattooStreamPosition [value=%s]", value);
	}
	
}
