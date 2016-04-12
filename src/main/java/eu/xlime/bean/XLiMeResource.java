package eu.xlime.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import eu.xlime.summa.bean.UIEntity;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@type")  
@JsonSubTypes({  
	    @Type(value = MicroPostBean.class, name = "http://rdfs.org/sioc/ns#MicroPost"),  
	    @Type(value = NewsArticleBean.class, name = "http://kdo.render-project.eu/kdo#NewsArticle"),
	    @Type(value = TVProgramBean.class, name = "http://www.w3.org/ns/ma-ont#MediaResource"),
	    @Type(value = VideoSegment.class, name="http://xlime.eu/vocab/MediaResourceSegment"),
	    @Type(value = ASRAnnotation.class, name="http://xlime.eu/vocab/ASRAnnotation"),	    
	    @Type(value = OCRAnnotation.class, name="http://xlime.eu/vocab/OCRAnnotation"),
	    @Type(value = SubtitleSegment.class, name="http://xlime.eu/vocab/SubtitleSegment"),
	    @Type(value = EREvent.class, name = "http://rdfs.org/sioc/ns#MicroPost"),
	    @Type(value = UIEntity.class, name = "http://xlime.eu/vocab/kbEntity"),
	    @Type(value = SearchString.class, name = "http://xlime.eu/vocab/searchString")
	    }) 
@XmlSeeAlso({MicroPostBean.class, NewsArticleBean.class, TVProgramBean.class})
//@XmlDiscriminatorNode("@type")
/**
 * The main interface for all data types known by the xLiMe Front-end Services.
 * @author RDENAUX
 *
 */
public interface XLiMeResource extends Serializable {

}