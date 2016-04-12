package eu.xlime.util;

import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.EREvent;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SearchString;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.summa.bean.UIEntity;

/**
 * Provides methods for determining the type of an {@link XLiMeResource} based on its URI
 * 
 * @author RDENAUX
 *
 */
public class ResourceTypeResolver {

	public Class<? extends XLiMeResource> resolveType(String uri) {
		if (isNewsArticle(uri)) return NewsArticleBean.class;
		else if (isMicroPost(uri)) return MicroPostBean.class;
		else if (isTVProgram(uri)) return TVProgramBean.class;
		else if (isKBEntity(uri)) return UIEntity.class;
		else if (isEREvent(uri)) return EREvent.class;
		else if (isASRAnnotation(uri)) return ASRAnnotation.class;
		else if (isOCRAnnotation(uri)) return OCRAnnotation.class;
		else if (isVideoSegment(uri)) return VideoSegment.class;
		else if (isSearchString(uri)) return SearchString.class;
		else throw new RuntimeException("Could not determine xLiMe Resource type for " + uri);
	}
	
	public boolean isNewsArticle(String uri) {
		return uri.startsWith("http://ijs.si/article/");
	}
	
	public boolean isMicroPost(String uri) {
		return uri.startsWith("http://vico-research.com/social/");
	}
	
	public boolean isTVProgram(String uri) {
		return uri.startsWith("http://zattoo.com/program/");
	}
	
	public boolean isKBEntity(String uri) {
		/* TODO: currently only support dbpedia, and only "default" dbpedia, not
		 *  any of the language dependent versions. 
		 * wikidata could also be an option, but we need a summa service for it
		 * wikipedia is not considered a KBEntity, see KBEntityMapper to go from a 
		 * 	wikipedia page to dbpedia
		 */
		return uri.startsWith("http://dbpedia.org/"); 
	}
	
	public boolean isEREvent(String uri) {
		return false; //TODO: implement
	}
	
	public boolean isASRAnnotation(String uri) {
		return false; //TODO: implement
	}
	
	public boolean isOCRAnnotation(String uri) {
		return false; //TODO: implement
	}
	
	public boolean isVideoSegment(String uri) {
		return false; //TODO: implement and make sure it differs from isTVProgram
	}
	
	public boolean isSearchString(String uri) {
		return false; //TODO: implement
	}
}
