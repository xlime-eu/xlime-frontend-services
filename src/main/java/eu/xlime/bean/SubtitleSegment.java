package eu.xlime.bean;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The contents of some subtitle stream that is part of a {@link VideoSegment}.
 * 
 * @author RDENAUX
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SubtitleSegment implements XLiMeResource {

	private static final long serialVersionUID = -447778998496127112L;

	@Id
	private String url;

	private VideoSegment partOf;
	
	private String text;
	
	private String lang;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public VideoSegment getPartOf() {
		return partOf;
	}

	public void setPartOf(VideoSegment partOf) {
		this.partOf = partOf;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	@Override
	public String toString() {
		return String.format(
				"SubtitleSegment [url=%s, partOf=%s, text=%s, lang=%s]", url,
				partOf, text, lang);
	}

}
