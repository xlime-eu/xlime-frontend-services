package eu.xlime.util;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

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
	public void testDate() throws Exception {
		ISO8601DateFormat format = new ISO8601DateFormat();
		Date d = new Date();
		final long timestamp = 1476796867500L;
		d.setTime(timestamp);
		
		Date expected = format.parse("2016-10-18T13:21:07Z");
		
		System.out.println("Startdate " + format.format(d));
		assertSametime(expected, d);
		
		long position = 100763761;
		calcStartPosDate(format, timestamp, position);
		
		calcStartPosDate(format, 1476796023500L, 100763550L);
		
		calcStartPosDate(format, 1475611567500L, 100467436L);
	}

	private void calcStartPosDate(ISO8601DateFormat format,
			final long expectedTimestamp, long position) throws Exception {
//		1000*(xlime:hasStreamPosition * '4seconds' + '2004-01-10 13:37:03.5'),
		Date blazDate = format.parse("2004-01-10T13:37:03Z");
		long blazDateS = (blazDate.getTime() + 500L) / 1000L;
		System.out.println("positionStartSecs " + blazDateS);
		long positionAsTimestamp = 1000 * (position * 4 + blazDateS);
		Date expDate = new Date();
		expDate.setTime(positionAsTimestamp);
		System.out.println("ExpDate: " + format.format(expDate));
		
		assertSametime(expectedTimestamp, positionAsTimestamp);		
	}
	
	private void assertSametime(Date expected, Date d) {
		assertSametime(expected.getTime(), d.getTime());
	}
	
	private void assertSametime(long expTimestamp, long timestamp) {
		double exp = expTimestamp / 1000.0;
		double db = timestamp / 1000.0;
		assertEquals(exp, db, 0.5);
	}

	@Test
	public void testResolveType() {
		ResourceTypeResolver testObj = new ResourceTypeResolver();
		assertEquals(NewsArticleBean.class, testObj.resolveType("http://ijs.si/article/367691523"));
		assertEquals(TVProgramBean.class, testObj.resolveType("http://zattoo.com/program/111364459"));
		assertEquals(MicroPostBean.class, testObj.resolveType("http://vico-research.com/social/c2f2c951-ecea-36fd-bc7d-35f97b736939"));	
		assertEquals(SearchString.class, testObj.resolveType("http://xlime.eu/vocab/search?q=Refugee"));
		assertEquals(UIEntity.class, testObj.resolveType("http://dbpedia.org/resource/Berlin"));
		assertEquals(SubtitleSegment.class, testObj.resolveType("http://zattoo.com/program/111364459/subtitles/111364459/111364460"));
		assertEquals(SubtitleSegment.class, testObj.resolveType("http://zattoo.com/program/-20/subtitles/100517176/100557176"));
		assertEquals(VideoSegment.class, testObj.resolveType("http://zattoo.com/program/111364459/111364459/111364460"));		
		//TODO: test other cases EREvent, ASR, OCR, 
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
		
		assertEquals("http://zattoo-production-zapi-sandbox.zattoo.com/watch/deutsche-welle/113684648/1451925000000/1451928600000/13908727000", testObj.toWatchUrl(vs));
	}
}
