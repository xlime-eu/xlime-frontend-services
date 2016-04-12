package eu.xlime.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.xlime.bean.SearchString;

public class SearchStringFactoryTest {

	@Test
	public void testFromUri() {
		String[] values = {
			"token1 token2",
			"tok(tok2)"
		};
		SearchStringFactory testObj = new SearchStringFactory();
		for (String value: values) {
			SearchString ss = new SearchString();
			ss.setValue(value);
			testRoundtripUri(testObj, ss);
		}
	}

	private void testRoundtripUri(SearchStringFactory testObj, SearchString ss) {
		String uri = testObj.toURI(ss).toString();
		SearchString ss2 = testObj.fromUri(uri);
		assertEquals(ss, ss2);
	}
}
