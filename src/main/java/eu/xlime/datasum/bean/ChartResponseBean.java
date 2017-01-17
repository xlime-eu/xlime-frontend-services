package eu.xlime.datasum.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChartResponseBean implements Serializable {
		
	private List<TimelineChart> timelines = new ArrayList<>();
	
	private List<String> errors = new ArrayList<>();

	public final List<TimelineChart> getTimelines() {
		return timelines;
	}

	public final void addTimeline(TimelineChart timeline) {
		this.timelines.add(timeline);
	}

	public final void addTimeline(Collection<? extends TimelineChart> timeline) {
		this.timelines.addAll(timeline);
	}
	
	public final void setTimelines(List<TimelineChart> timelines) {
		this.timelines = timelines;
	}

	public final List<String> getErrors() {
		return errors;
	}

	public final void addError(String error) {
		this.errors.add(error);
	}
	
	public final void setErrors(List<String> errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		return String.format("ChartResponseBean [timelines=%s, errors=%s]",
				timelines, errors);
	}
	
}
