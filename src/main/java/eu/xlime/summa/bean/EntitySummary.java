package eu.xlime.summa.bean;

import java.util.List;


/**
 * Represents the summary of a given {@link #entity}, which consist of a number 
 * (less or equal to {@link #topK}) of {@link #statements} which 
 * <it>summarise</it> the #entity.
 * 
 * @author RDENAUX
 *
 */
public class EntitySummary {

	private UIEntity entity;
	
	private String type = "http://example.com/entitySummary";
	
	private List<SummaryStatement> statements;
	
	private int topK;

	public UIEntity getEntity() {
		return entity;
	}

	public void setEntity(UIEntity entity) {
		this.entity = entity;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<SummaryStatement> getStatements() {
		return statements;
	}

	public void setStatements(List<SummaryStatement> statements) {
		this.statements = statements;
	}

	public int getTopK() {
		return topK;
	}

	public void setTopK(int topK) {
		this.topK = topK;
	}

	@Override
	public String toString() {
		return "EntitySummary [entity=" + entity + ", type=" + type
				+ ", statements=" + statements + ", topK=" + topK + "]";
	}

}
