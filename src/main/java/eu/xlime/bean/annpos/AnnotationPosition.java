package eu.xlime.bean.annpos;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@type")  
@JsonSubTypes({  
	    @Type(value = SpanInTextPosition.class, name = "http://xlime.eu/vocab/SpanInText"),  
	    }) 
/**
 * Generic interface for objects which denote the position of some annotation in 
 * a resource. Obviously, the type of information depends on the type of annotation and
 * the type of resource that was annotated (e.g. video, text, an image, etc.)
 * 
 * @author rdenaux
 *
 */
public interface AnnotationPosition extends Serializable {

}
