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
	public List<EntityAnnotation> findAudioTrackEntityAnnotations(
			String audioTrackUrl) {
		return delegate.findAudioTrackEntityAnnotations(audioTrackUrl);
	}

	@Override
	public ScoredSet<String> findMediaItemUrlsByKBEntity(String entityUrl) {
		return delegate.findMediaItemUrlsByKBEntity(entityUrl);
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
