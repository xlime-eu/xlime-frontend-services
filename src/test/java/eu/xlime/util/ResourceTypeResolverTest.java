package eu.xlime.util;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;

import org.junit.Test;

import eu.xlime.bean.Content;
import eu.xlime.bean.Duration;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.SearchString;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.UIDate;
import eu.xlime.bean.UrlLabel;
import eu.xlime.bean.VideoSegment;
import eu.xlime.bean.ZattooStreamPosition;
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
	
	@Test
	public void test_toWatchUrl_VideoSegment() throws Exception {
		ResourceTypeResolver testObj = new ResourceTypeResolver();

		VideoSegment vs = new VideoSegment();
		vs.setUrl("http://zattoo.com/program/113684536/2342/2342");
		ZattooStreamPosition pos = new ZattooStreamPosition();
		pos.setValue(98022976);
		vs.setPosition(pos);
		vs.setStartTime(null);
		
		TVProgramBean progBean = new TVProgramBean();
		progBean.setBroadcastDate(new UIDate(new SimpleDateFormat("YYYY-MM-DD'T'hh:mm:ss").parse("2016-06-13T17:30:00")));
		progBean.setDescription(new Content());
		progBean.setDuration(new Duration(3600));
		UrlLabel pub = new UrlLabel();
		pub.setLabel("Deutsche Welle");
		progBean.setPublisher(pub);
		progBean.setTitle("heute leben");
		progBean.setUrl("http://zattoo.com/program/113684648");
		vs.setPartOf(progBean);
		
		assertEquals("http://zattoo-production-zapi-sandbox.zattoo.com/watch/deutsche-welle/113684648/1451925000000/1451928600000/1443963727500", testObj.toWatchUrl(vs));
//		assertEquals("http://zattoo-production-zapi-sandbox.zattoo.com/watch/deutsche-welle/113684648/1451925000000/1451928600000/1443920527500", testObj.toWatchUrl(vs));
	}
}
