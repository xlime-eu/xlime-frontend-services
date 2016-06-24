package eu.xlime.dao.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import eu.xlime.bean.Content;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.UIDate;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.util.KBEntityMapper;

public class SparqlMediaItemAnnotationDaoTest {

	@Test
	public void testToOCRAnnotation() throws Exception {
		SparqlMediaItemAnnotationDao testObj = new MockSparqlMediaItemAnnotationDao();
		Map<String, String> mockTuple = ImmutableMap.of(
				"ocr", "\n96742164.000000, 07:43\n,", //
				"vidTrack", "http://zattoo.com/program/112105091/video");
		TVProgramBean mockTVProg = new TVProgramBean();
		mockTVProg.setUrl("http://zattoo.com/program/112105091");
		UIDate broadcastDate = new UIDate(new Date());
		mockTVProg.setBroadcastDate(broadcastDate);
		Content content = new Content();
		content.setFull("Matt Allwright investigates the con men who are trying to get their hands on your money by using fakes, forgeries and frauds and shows you how to avoid being taken for a ride. In this episode, Matt reveals the fake fundraising websites stealing money from those who need it most, and investigates the fake horse passports putting young riders in danger. Plus the story of the furniture that is a fire hazard - how a Fake Britain investigation led to a national recall of illegal and dangerous mattresses.");
		mockTVProg.setDescription(content);
		
		Optional<OCRAnnotation> optAnn = testObj.toOCRAnnotation(mockTuple, mockTVProg);
		assertTrue(optAnn.isPresent());
		System.out.println("ocrAnn: " + optAnn.get());
		assertNotNull(optAnn.get().getInSegment());
		assertEquals(" 07:43\n,", optAnn.get().getRecognizedText());		
	}
	
	
	static class MockSparqlMediaItemAnnotationDao extends SparqlMediaItemAnnotationDao {

		@Override
		protected SparqlClient getXliMeSparqlClient() {
			return null;
		}

		@Override
		protected KBEntityMapper getKBEntityMapper() {
			return null;
		}
		
	}
}
