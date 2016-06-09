package eu.xlime.summa.bean;

import java.io.Serializable;

import eu.xlime.bean.UrlLabel;

/**
 * Ranked Tuple that represents a statement (triple) relative to some tacit entity. 
 * The direction of the relation is also implicit, only indicated by the 
 * <code>label</code> of the relation.
 *  
 * For example, if you want to summarise Berlin you'd  need see something like:
 * <code>
 *   SummaryStatement(
 *     UrlLabel("http://example.com/property/country", "country"), //relation
 *     UrlLabel("http://example.com/entity/Germany", "Germany"), //related
 *     0.9); //rankValue
 * </code>
 * which comes from an original triple <code>Berlin country Germany</code>.
 * 
 * For an <it>inverse</it> relation <code>Europe cities Berlin</code>, you'd get
 * something like: 
 * <code>
 *   SummaryStatement(
 *     UrlLabel("http://example.com/property/cities", "cities of"), //relation (inverse!!)
 *     UrlLabel("http://example.com/entity/Europe", "Europe"), //related
 *     0.89); //rankValue 
 * </code>
 * 
 * Also, note that the {@link #related} object can also be a literal (or a blank node), in both
 * cases, the {@link UrlLabel} would only contain the label, and the url would be <code>null</code>. E.g.
 * 
 * <code>
 *   SummaryStatement(
 *     UrlLabel("http://example.com/property/population", "population"), //relation
 *     UrlLabel(null, "3562166"), //related
 *     0.43); //rankValue
 * </code>
 *  
 * @author RDENAUX
 *
 */
public class SummaryStatement implements Serializable {

	private static final long serialVersionUID = 8433822080888925009L;

	private UrlLabel relation;
	
	private UrlLabel related;
	
	private double rankValue;

	public UrlLabel getRelation() {
		return relation;
	}

	public void setRelation(UrlLabel relation) {
		this.relation = relation;
	}

	public UrlLabel getRelated() {
		return related;
	}

	public void setRelated(UrlLabel related) {
		this.related = related;
	}

	public double getRankValue() {
		return rankValue;
	}

	public void setRankValue(double rankValue) {
		this.rankValue = rankValue;
	}

	@Override
	public String toString() {
		return "SummaryStatement [relation=" + relation + ", related="
				+ related + ", rankValue=" + rankValue + "]";
	}
	
	
}
