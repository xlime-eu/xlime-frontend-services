package eu.xlime.util.score;

import com.google.common.base.Optional;

public class JustificationBuilder {

	StringBuilder main = new StringBuilder();
	StringBuilder subs = new StringBuilder();
	
	public JustificationBuilder add(String mainJustif) {
		main.append(mainJustif);
		return this;
	}
	
	public JustificationBuilder addSubJustification(String subJustification) {
		for (String subLine: subJustification.split("\n")) {
			subs.append("\n\t").append(subLine);
		}
		return this;
	}
	
	public String build() {
		return main.toString() + subs.toString(); 
	}

	public JustificationBuilder addSubJustification(Optional<String> justification) {
		if (justification.isPresent()) addSubJustification(justification.get());
		return this;
	}
}
