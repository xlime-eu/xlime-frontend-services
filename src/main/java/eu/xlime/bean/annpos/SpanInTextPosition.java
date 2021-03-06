package eu.xlime.bean.annpos;


public class SpanInTextPosition implements AnnotationPosition {

	private static final long serialVersionUID = -489911253051763078L;
	
	private long start;
	private long end;
	
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}
	@Override
	public String toString() {
		return "AnnotationPosition [start=" + start + ", end=" + end + "]";
	}
	
}
