package eu.xlime.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@type")  
@JsonSubTypes({  
	    @Type(value = ZattooStreamPosition.class, name = "http://xlime.eu/vocab/ZattooStreamPosition")  
	    }) 
/**
 * Contains information about where a {@link VideoSegment} is located within a {@link TVProgramBean}.
 * Depending on the type of {@link TVProgramBean} and {@link VideoSegment}, this may be implemented 
 * differently.
 *  
 * @author rdenaux
 *
 */
public interface VideoSegmentPosition extends Serializable {

}
