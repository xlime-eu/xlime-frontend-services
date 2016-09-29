package eu.xlime.dao.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.ParsePosition;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.junit.Test;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.base.Optional;
import com.hp.hpl.jena.query.Dataset;

import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.testkit.DatasetLoader;
import eu.xlime.util.KBEntityMapper;
import eu.xlime.util.NullEnDBpediaKBEntityMapper;

public class MediaItemAnnotationDaoFromDatasetTest {

	private static DatasetLoader dsLoader = new DatasetLoader();

	@Test
	public void testFindAllSubtitleSegmentsByDate() throws Exception {
		MediaItemAnnotationDaoFromDataset inst = createTestMediaItemDaoFromDataset("src/test/resources/zattoo-sub-example-graph.trig");

		long dateFrom = ISO8601Utils.parse("2016-06-13T00:00:00Z", new ParsePosition(0)).getTime();
		long dateTo =   ISO8601Utils.parse("2016-06-13T20:00:00Z", new ParsePosition(0)).getTime();
		List<SubtitleSegment> sts = inst.findAllSubtitleSegmentsByDate(dateFrom, dateTo, 50);
		
		assertNotNull(sts);
		System.out.println(String.format("Found %s subtitles %s", sts.size(), sts));
		assertEquals(5, sts.size());
	}

	@Test
	public void testFindSubtitleSegments() throws Exception {
		MediaItemAnnotationDaoFromDataset inst = createTestMediaItemDaoFromDataset("src/test/resources/zattoo-sub-example-graph.trig");
		
		List<SubtitleSegment> sts = inst.findSubtitleSegmentsForTVProg("http://zattoo.com/program/113684536");
		
		assertNotNull(sts);
		System.out.println(String.format("Found %s subtitles %s", sts.size(), sts));
		assertEquals(2, sts.size()); //there are repeated values in the input trig file
	}

	@Test
	public void testFindAllSubtitleSegments() throws Exception {
		MediaItemAnnotationDaoFromDataset inst = createTestMediaItemDaoFromDataset("src/test/resources/zattoo-sub-example-graph.trig");
		
		List<SubtitleSegment> sts = inst.findAllSubtitleSegments(50);
		
		assertNotNull(sts);
		System.out.println(String.format("Found %s subtitles %s", sts.size(), sts));
		assertEquals(5, sts.size());
	}
	
	@Test
	public void testFindMicroPostEntityAnnotations() throws Exception {
		MediaItemAnnotationDaoFromDataset inst = createTestMediaItemDaoFromDataset("src/test/resources/socmedia-example-graph.trig");
		
		List<EntityAnnotation> eas = inst.findMicroPostEntityAnnotations("http://vico-research.com/social/181cb803-a680-334d-a419-556272878526");
		
		assertNotNull(eas);
		System.out.println(String.format("Found %s entity annots %s", eas.size(), eas));
		assertEquals(4, eas.size());
		assertNotNull(eas.get(0).getPosition());
	}
	
	@Test
	public void testFindNewsArticleEntityAnnotations() throws Exception {
		MediaItemAnnotationDaoFromDataset inst = createTestMediaItemDaoFromDataset("src/test/resources/jsi-newsfeed-example-graph.trig");

		List<EntityAnnotation> eas = inst.findNewsArticleEntityAnnotations("http://ijs.si/article/454914915");
		
		assertNotNull(eas);
		System.out.println(String.format("Found %s entity annots %s", eas.size(), eas));
		assertEquals(14, eas.size());
		
	}
	
	@Test
	public void testFindSubtitleTrackEntityAnnotations() throws Exception {
		MediaItemAnnotationDaoFromDataset inst = createTestMediaItemDaoFromDataset("src/test/resources/zattoo-sub-example-graph.trig");

		List<EntityAnnotation> eas = inst.findSubtitleTrackEntityAnnotations("http://zattoo.com/program/113684648/subtitles");
		
		assertNotNull(eas);
		System.out.println(String.format("Found %s entity annots %s", eas.size(), eas));
		assertEquals(3, eas.size());
	}
	
	@Test
	public void test_findOCRAnnotationsFor() throws Exception {
		MediaItemAnnotationDaoFromDataset inst = createTestMediaItemDaoFromDataset("src/test/resources/tv-ocr-example-graph.trig");

		TVProgramBean tvb = new TVProgramBean();
		tvb.setUrl("http://zattoo.com/program/114078287");
		List<OCRAnnotation> oas = inst.findOCRAnnotationsFor(tvb);
		System.out.println(String.format("Found %s ocr annots %s", oas.size(), oas));
		assertEquals(4, oas.size());
	}

	@Test
	public void test_allOCRAnnotations() throws Exception {
		MediaItemAnnotationDaoFromDataset inst = createTestMediaItemDaoFromDataset("src/test/resources/tv-ocr-example-graph.trig");

		List<OCRAnnotation> oas = inst.findAllOCRAnnotations(200);
		System.out.println(String.format("Found %s ocr annots %s", oas.size(), oas));
		assertEquals(4, oas.size());
	}
	
	private MediaItemAnnotationDaoFromDataset createTestMediaItemDaoFromDataset(String fpath) {
		Optional<Dataset> ds = dsLoader.loadDataset(new File(fpath), Lang.TRIG);
		assertTrue(ds.isPresent());

		KBEntityMapper kbEntMapper = new NullEnDBpediaKBEntityMapper();//new FstEnDBpediaEntityMapper(new File("src/test/resources/interlang_links_en_100K.ttl"), new File("src/test/resources/supportedOtherDomains.txt"));
		MediaItemAnnotationDaoFromDataset testObj = new MediaItemAnnotationDaoFromDataset(ds.get(), kbEntMapper);
		return testObj;
	}
	
	static public List<SubtitleSegment> findTestSubtitleSegments() {
		MediaItemAnnotationDaoFromDatasetTest inst = new MediaItemAnnotationDaoFromDatasetTest();
		MediaItemAnnotationDaoFromDataset testObj = inst.createTestMediaItemDaoFromDataset("src/test/resources/zattoo-sub-example-graph.trig");
		List<SubtitleSegment> segs = testObj.findAllSubtitleSegments(100);
		return segs;
	}
	
}
