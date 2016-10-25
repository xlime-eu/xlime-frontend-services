package eu.xlime.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@type")  
@JsonSubTypes({  
	    @Type(value = ZattooCustomTVInfo.class, name = "http://xlime.eu/vocab/ZattooCustomTVInfo")  
	    }) 
/**
 * Contains specific information about where a {@link TVProgramBean} that is not generic for tv-programs,
 * but may be specific to a particular provider of TV programs (e.g. Zattoo).
 *  
 * @author rdenaux
 *
 */
public interface CustomTVInfo extends Serializable {

}
