package eu.xlime.dao;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;

import eu.xlime.bean.SearchString;

public class SearchStringFactory {

	public static final String baseUrl = "http://xlime.eu/vocab/search?q=";
	
	/**
	 * Creates a SearchString based on a uri
	 * @param uri
	 * @return
	 */
	public SearchString fromUri(String uri) {
		try {
			String dUri = URLDecoder.decode(uri, "UTF-8");
			String s = dUri.substring(baseUrl.length());
			SearchString result = new SearchString();
			result.setValue(s);
			return result;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public URI toURI(SearchString ss) {
		try {
			return URI.create(baseUrl + URLEncoder.encode(ss.getValue(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
