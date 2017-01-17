package eu.xlime.dao.annotation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import eu.xlime.Config;
import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.dao.MediaItemAnnotationDao;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.score.ScoredSet;

public class MediaItemAnnotationDaoImpl extends AbstractMediaItemAnnotationDao {

	private static final Logger log = LoggerFactory.getLogger(MediaItemAnnotationDaoImpl.class);
	
	private MediaItemAnnotationDao delegate;
	
	public MediaItemAnnotationDaoImpl() {
//		delegate = new XLiMeSparqlMediaItemAnnotationDao();
		delegate = new MongoMediaItemAnnotationDao(new Config().getCfgProps());
	}

	@Override
	public List<XLiMeResource> findRecentAnnotations(int minutes, int limit) {
		return delegate.findRecentAnnotations(minutes, limit);
	}

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemAnnotationDao#findMicroPostEntityAnnotations(java.lang.String)
	 */
	@Override
	public List<EntityAnnotation> findMicroPostEntityAnnotations(final String url) {
		return delegate.findMediaItemEntityAnnotations(url);
	}
	
	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemAnnotationDao#findNewsArticleEntityAnnotations(java.lang.String)
	 */
	@Override
	public List<EntityAnnotation> findNewsArticleEntityAnnotations(final String url) {
		return delegate.findNewsArticleEntityAnnotations(url);
	}
	
	@Override
	public List<EntityAnnotation> findEntityAnnotationsFor(UIEntity kbEntity) {
		return delegate.findEntityAnnotationsFor(kbEntity);
	}

	@Override
	public List<EntityAnnotation> findSubtitleTrackEntityAnnotations(
			String subtitleTrackUrl) {
		return delegate.findSubtitleTrackEntityAnnotations(subtitleTrackUrl);
	}

	@Override
	public List<EntityAnnotation> findTVSubtitleEntityAnnotations(
			String tvProgUrl) {
		return delegate.findTVSubtitleEntityAnnotations(tvProgUrl);
	}

	@Override
	public List<EntityAnnotation> findSubtitleEntityAnnotations(String subUrl) {
		return delegate.findSubtitleEntityAnnotations(subUrl);
	}

	@Override
	public List<EntityAnnotation> findAudioTrackEntityAnnotations(
			String audioTrackUrl) {
		return delegate.findAudioTrackEntityAnnotations(audioTrackUrl);
	}
	
	@Override
	public List<EntityAnnotation> findTVASREntityAnnotations(String tvProgUrl) {
		return delegate.findTVASREntityAnnotations(tvProgUrl);
	}

	@Override
	public List<EntityAnnotation> findASREntityAnnotations(String asrUrl) {
		return delegate.findASREntityAnnotations(asrUrl);
	}

	@Override
	public List<EntityAnnotation> findVideoTrackEntityAnnotations(
			String videoTrackUrl) {
		return delegate.findVideoTrackEntityAnnotations(videoTrackUrl);
	}

	@Override
	public ScoredSet<String> findMediaItemUrlsByKBEntity(String entityUrl) {
		return delegate.findMediaItemUrlsByKBEntity(entityUrl);
	}

	@Override
	public ScoredSet<ASRAnnotation> findASRAnnotationsForKBEntity(
			String entityUrl) {
		return delegate.findASRAnnotationsForKBEntity(entityUrl);
	}

	@Override
	public ScoredSet<OCRAnnotation> findOCRAnnotationForKBEntity(
			String entityUrl) {
		return delegate.findOCRAnnotationForKBEntity(entityUrl);
	}

	@Override
	public ScoredSet<SubtitleSegment> findSubtitleSegmentsForKBEntity(
			String entityUrl) {
		return delegate.findSubtitleSegmentsForKBEntity(entityUrl);
	}

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemAnnotationDao#findASRAnnotation(java.lang.String)
	 */
	@Override
	public Optional<ASRAnnotation> findASRAnnotation(String uri) {
		return delegate.findASRAnnotation(uri);
	}

	@Override
	public List<ASRAnnotation> findAllASRAnnotations(int limit) {
		return delegate.findAllASRAnnotations(limit);
	}

	@Override
	public List<ASRAnnotation> findASRAnnotationsByText(String text) {
		return delegate.findASRAnnotationsByText(text);
	}

	@Override
	public List<ASRAnnotation> findASRAnnotationsForTVProg(String tvProgUri) {
		return delegate.findASRAnnotationsForTVProg(tvProgUri);
	}

	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemAnnotationDao#findOCRAnnotationsFor(eu.xlime.bean.TVProgramBean)
	 */
	@Override
	public List<OCRAnnotation> findOCRAnnotationsFor(final TVProgramBean mediaResource) {
		return delegate.findOCRAnnotationsFor(mediaResource);
	}
	
	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemAnnotationDao#findOCRAnnotation(java.lang.String)
	 */
	@Override
	public Optional<OCRAnnotation> findOCRAnnotation(String ocrAnnotUri) {
		return delegate.findOCRAnnotation(ocrAnnotUri);
	}

	@Override
	public List<OCRAnnotation> findAllOCRAnnotations(int limit) {
		return delegate.findAllOCRAnnotations(limit);
	}

	@Override
	public List<OCRAnnotation> findOCRAnnotationsByText(String textQuery) {
		return delegate.findOCRAnnotationsByText(textQuery);
	}

	@Override
	public Optional<SubtitleSegment> findSubtitleSegment(
			String subtitleSegmentUri) {
		return delegate.findSubtitleSegment(subtitleSegmentUri);
	}

	@Override
	public List<SubtitleSegment> findSubtitleSegmentsForTVProg(String tvProgUri) {
		return delegate.findSubtitleSegmentsForTVProg(tvProgUri);
	}
	
	@Override
	public List<SubtitleSegment> findSubtitleSegmentsByText(String textQuery) {
		return delegate.findSubtitleSegmentsByText(textQuery);
	}

	@Override
	public List<SubtitleSegment> findAllSubtitleSegments(int limit) {
		return delegate.findAllSubtitleSegments(limit);
	}

	@Override
	public List<SubtitleSegment> findAllSubtitleSegmentsByDate(long dateFrom,
			long dateTo, int limit) {
		return delegate.findAllSubtitleSegmentsByDate(dateFrom, dateTo, limit);
	}

}
