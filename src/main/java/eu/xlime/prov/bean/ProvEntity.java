package eu.xlime.prov.bean;

import javax.persistence.Id;

import eu.xlime.bean.UrlLabel;

/**
 * Within xLiMe, a set of xLiMe data produced by some activity.
 *  
 * @author rdenaux
 *
 */
public class ProvEntity {

	/**
	 * The url for this provenance entity
	 */
	@Id
	private String url;
	
	private String title;
	
	private UrlLabel attributedTo;
	
	private ProvActivity generatedBy;
	
}
