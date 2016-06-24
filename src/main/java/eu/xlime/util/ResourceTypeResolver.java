package eu.xlime.util;

import com.google.common.base.Optional;

import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.EREvent;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SearchString;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.dao.SearchStringFactory;
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
		if (isMicroPost(uri)) return MicroPostBean.class;
		if (isTVProgram(uri)) return TVProgramBean.class;
		if (isKBEntity(uri)) return UIEntity.class;
		if (isEREvent(uri)) return EREvent.class;
		if (isASRAnnotation(uri)) return ASRAnnotation.class;
		if (isOCRAnnotation(uri)) return OCRAnnotation.class;
		if (isVideoSegment(uri)) return VideoSegment.class;
		if (isSearchString(uri)) return SearchString.class;
		if (isSubtitleSegment(uri)) return SubtitleSegment.class;
		throw new RuntimeException("Could not determine xLiMe Resource type for " + uri);
	}
	
	private boolean isSubtitleSegment(String uri) {
		// subtitles for zattoo program with start offset and an end offset
		return uri.matches("http://zattoo.com/program/\\d+/subtitles/\\d+/\\d+"); 
	}
	
	public boolean isMediaItem(String uri) {
		return (isMicroPost(uri) || isNewsArticle(uri) || isTVProgram(uri));
	}

	public String extractSubtitleTrackUri(SubtitleSegment subtitSeg) {
		int i = subtitSeg.getUrl().indexOf("/subtitles/");
		if (i < 0) throw new RuntimeException("Unexpected subtitleSegment url" + subtitSeg);
		return subtitSeg.getUrl().substring(0, i) + "/subtitles";
	}
	
	public boolean isNewsArticle(String uri) {
		return uri.startsWith("http://ijs.si/article/");
	}
	
	public boolean isMicroPost(String uri) {
		return uri.startsWith("http://vico-research.com/social/");
	}
	
	public boolean isTVProgram(String uri) {
		return uri.matches("http://zattoo.com/program/\\d+");
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
		// the program with progId, startTime and endTime offsets
		return uri.matches("http://zattoo.com/program/\\d+/\\d+/\\d+"); 
	}
	
	public boolean isSearchString(String uri) {
		return uri.startsWith(SearchStringFactory.baseUrl);
	}

	public boolean isSubtitleTrack(String uri) {
		return uri.matches("http://zattoo.com/program/\\d+/subtitles"); 
	}

	public Optional<String> subtitleTrackUrlAsTVProgUrl(String uri) {
		if (!isSubtitleTrack(uri)) throw new IllegalArgumentException("Uri is not a subtitle track url " + uri);
		return Optional.of(uri.replaceAll("/subtitles", ""));
	}
	
	
}
