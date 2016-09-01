package eu.xlime.dao.annotation;

import static org.junit.Assert.*;

import java.text.ParsePosition;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.base.Optional;

import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.dao.mediaitem.MongoMediaItemDao;
import eu.xlime.mongo.ConfigOptions;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.ResourceTypeResolver;
import eu.xlime.util.score.ScoredSet;

public class MongoMediaItemAnnotationDaoITCase {

	@Test
	public void testFindEntityAnnotations() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();


		List<EntityAnnotation> result = dao.findEntityAnnotations(100);
		
		System.out.println("Retrieved EntAnns " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
		assertEquals(100, result.size());
	}
	
	@Test
	public void testFindMicroPostEntityAnnotations() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		String miUrl = "http://vico-research.com/social/69a16da8-9be4-334f-ac40-b8cd7a416095";
		List<EntityAnnotation> result = dao.findMicroPostEntityAnnotations(miUrl);
		
		System.out.println("Retrieved microPostEntAnns " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}

	@Test
	public void testFindNewsArticleEntityAnnotations() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		String miUrl = "http://ijs.si/article/454936930";
		List<EntityAnnotation> result = dao.findNewsArticleEntityAnnotations(miUrl);

		System.out.println("Retrieved newsEntAnns " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}

	@Test
	public void testFindSubtitleTrackEntityAnnotations() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		String stUrl = "";
		List<EntityAnnotation> result = dao.findSubtitleTrackEntityAnnotations(stUrl);

		System.out.println("Retrieved subTitEntAnns " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}

	@Test
	public void testFindEntityAnnotationsForKBEntity() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		UIEntity kbEnt = new UIEntity();
		kbEnt.setUrl("http://dbpedia.org/resource/David_Cameron");
		List<EntityAnnotation> result = dao.findEntityAnnotationsFor(kbEnt);

		System.out.println("Retrieved EntAnns " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}

	@Test
	public void testFindMediaItemUrlsByKBEntity() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		String kbEntUri = "http://dbpedia.org/resource/David_Cameron";
		ScoredSet<String> result = dao.findMediaItemUrlsByKBEntity(kbEntUri);

		System.out.println("Retrieved media item urls " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}
	
	@Test
	public void testFindASRAnnotation() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		String miUrl = "";
		Optional<ASRAnnotation> result = dao.findASRAnnotation(miUrl);

		System.out.println("Retrieved ASRAnns " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}

	@Test
	public void testFindOCRAnnotationsFor() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		TVProgramBean tvProgBean = null;
		List<OCRAnnotation> result = dao.findOCRAnnotationsFor(tvProgBean);

		System.out.println("Retrieved OCRAnns " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}

	@Test
	public void testFindOCRAnnotation() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		String ocraUri = "";
		Optional<OCRAnnotation> result = dao.findOCRAnnotation(ocraUri);

		System.out.println("Retrieved OCRAnns " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}

	@Test
	public void testFindSubtitleSegmentsForTVProg() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		String tvpUri = "http://zattoo.com/program/113843736";
		List<SubtitleSegment> result = dao.findSubtitleSegmentsForTVProg(tvpUri);

		System.out.println(String.format("Retrieved %s subtitSegss in %s ms: %s", result.size(), (System.currentTimeMillis() - start), result));
		assertNotNull(result);
		if (result.size() > 0) {
			SubtitleSegment seg = result.get(0);
			testSegmentWatchUrl(seg);
		}
	}

	private void testSegmentWatchUrl(SubtitleSegment seg) {
		System.out.println("Testing watch url for " + seg);
		MongoMediaItemDao miDao = createTestMediaItemDao();
		Optional<TVProgramBean> otvp = miDao.findTVProgram(seg.getPartOf().getPartOf().getUrl());
		System.out.println("Retrieved " + otvp);
		seg.getPartOf().setPartOf(otvp.get());
		ResourceTypeResolver typeReso = new ResourceTypeResolver();
		assertEquals("http://zattoo-production-zapi-sandbox.zattoo.com/watch/bbc-one/113843736/1466420700000/1466425800000/14", typeReso.toWatchUrl(seg.getPartOf()));
	}
	
	@Test
	public void testFindSubtitleSegmentsByText() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		String query = "\"Jo Cox\"";
		List<SubtitleSegment> result = dao.findSubtitleSegmentsByText(query);

		System.out.println("Retrieved SubtitSegs " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}
	
	@Test
	public void testFindAllSubtitleSegments() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		List<SubtitleSegment> result = dao.findAllSubtitleSegments(30);

		System.out.println("Retrieved SubtitSegs " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
		if (result.size() > 0) {
			SubtitleSegment seg = result.get(result.size() - 1);
			testSegmentWatchUrl(seg);
		}
	}

	private MongoMediaItemAnnotationDao createTestObj() {
		Properties props = new Properties();
		props.put(ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getKey(), "xlimeres");
		MongoMediaItemAnnotationDao dao = new MongoMediaItemAnnotationDao(props);
		return dao;
	}

	private MongoMediaItemDao createTestMediaItemDao() {
		Properties props = new Properties();
		props.put(ConfigOptions.XLIME_MONGO_RESOURCE_DATABASE_NAME.getKey(), "xlimeres");
		MongoMediaItemDao dao = new MongoMediaItemDao(props);
		return dao;
	}
	
	@Test
	public void testFindAllSubtitleSegmentsByDate() throws Exception {
		MongoMediaItemAnnotationDao dao = createTestObj();
		long start = System.currentTimeMillis();

		long dateFrom = ISO8601Utils.parse("2016-06-18T07:00:00Z", new ParsePosition(0)).getTime();
		long dateTo =   ISO8601Utils.parse("2016-06-18T08:00:00Z", new ParsePosition(0)).getTime();
		List<SubtitleSegment> result = dao.findAllSubtitleSegmentsByDate(dateFrom, dateTo, 30);

		System.out.println("Retrieved SubtitSegs " + result + " in " + (System.currentTimeMillis() - start) + "ms.");
		assertNotNull(result);
	}

}
