package eu.xlime.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@type")  
@JsonSubTypes({  
	    @Type(value = MicroPostBean.class, name = "http://rdfs.org/sioc/ns#MicroPost"),  
	    @Type(value = NewsArticleBean.class, name = "http://kdo.render-project.eu/kdo#NewsArticle"),
	    @Type(value = TVProgramBean.class, name = "http://www.w3.org/ns/ma-ont#MediaResource")}) 
@XmlSeeAlso({MicroPostBean.class, NewsArticleBean.class, TVProgramBean.class})
//@XmlDiscriminatorNode("@type")
public abstract class MediaItem implements Serializable {

}
