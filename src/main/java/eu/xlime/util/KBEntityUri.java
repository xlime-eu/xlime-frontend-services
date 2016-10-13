package eu.xlime.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Set;

import org.apache.jena.iri.IRIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class KBEntityUri {

	private static final Logger log = LoggerFactory.getLogger(KBEntityUri.class);
	
	private final String uri;

	public static KBEntityUri of(String uri) {
		return new KBEntityUri(uri);
	}
	
	public KBEntityUri(String uri) {
		super();
		this.uri = uri;
	}
	
	public final boolean isEncoded() {
		return !decodedUrl().equals(uri);
	}

	final String decodedUrl() {
		try {
			return URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public boolean isMainDBpediaEntity() {
		return uri.contains("http://dbpedia.org/");
	}
		
	public String asIri() {
		return IRIFactory.uriImplementation().create(decodedUrl()).toString();
	}
	
	public final boolean isWikiEnt() {
		return uri.matches("http://(\\w\\w).wikipedia.org/wiki/.*");
	}

	public final boolean isLangDependentDBpediaEntity() {
		return uri.matches("http://(\\w\\w).dbpedia.org/resource/.*");
	}
	
	public final Optional<String> rewriteWikiToDBpedia(Set<String> langWhitelist) {
		//TODO: better to use a regular expression to extract these values?
		String langDom = uri.substring(7, 9);
		if (!langWhitelist.isEmpty() && !langWhitelist.contains(langDom)) {
			log.debug("Not rewriting wiki to dbpedia for lang " + langDom);
			return Optional.absent();
		}
		langDom = langDom + ".";
		if (langDom.equals("en.")) langDom = "";
		String entName = uri.substring(29);
		return Optional.of(String.format("http://%sdbpedia.org/resource/%s", langDom, entName));
	}
	
	public Optional<String> rewriteDBpediaToWiki() {
		String langDom = "en.";
		if (isLangDependentDBpediaEntity()) {
			langDom = uri.substring(7, 9) + ".";
		}
		String detectSeg = "dbpedia.org/resource/";
		int index = uri.indexOf(detectSeg) + detectSeg.length();
		String entName = uri.substring(index);
		return Optional.of(String.format("http://%swikipedia.org/wiki/%s", langDom, entName));
	}

	public String labelFromUri() {
		int index = uri.lastIndexOf("/"); //TODO: better to extract using regular expression?
		String labelFromUri = (index > 0) ? uri.substring(index) : uri; 
		return labelFromUri;
	}
	
}
