package eu.xlime.util;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.SearchString;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.summa.bean.UIEntity;

public class ResourceTypeResolverTest {

	@Test
	public void testResolveType() {
		ResourceTypeResolver testObj = new ResourceTypeResolver();
		assertEquals(NewsArticleBean.class, testObj.resolveType("http://ijs.si/article/367691523"));
		assertEquals(TVProgramBean.class, testObj.resolveType("http://zattoo.com/program/111364459"));
		assertEquals(MicroPostBean.class, testObj.resolveType("http://vico-research.com/social/c2f2c951-ecea-36fd-bc7d-35f97b736939"));	
		assertEquals(SearchString.class, testObj.resolveType("http://xlime.eu/vocab/search?q=Refugee"));
		assertEquals(UIEntity.class, testObj.resolveType("http://dbpedia.org/resource/Berlin"));
		assertEquals(SubtitleSegment.class, testObj.resolveType("http://zattoo.com/program/111364459/subtitles/111364459/111364460"));
		assertEquals(VideoSegment.class, testObj.resolveType("http://zattoo.com/program/111364459/111364459/111364460"));		
		//TODO: test other cases EREvent, ASR, OCR, VideoSegment, 
	}


	@Test
	public void testExtractSubtitleTrackUrl() throws Exception {
		ResourceTypeResolver testObj = new ResourceTypeResolver();
		
		SubtitleSegment subtitSeg = new SubtitleSegment();
		subtitSeg.setUrl("http://zattoo.com/program/111364459/subtitles/111364459/111364460");
		assertEquals("http://zattoo.com/program/111364459/subtitles", testObj.extractSubtitleTrackUri(subtitSeg));
	}
}
