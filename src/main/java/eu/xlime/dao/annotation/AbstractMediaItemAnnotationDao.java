package eu.xlime.dao.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.bean.ZattooStreamPosition;
import eu.xlime.dao.MediaItemAnnotationDao;
import eu.xlime.dao.MediaItemDao;
import eu.xlime.util.ResourceTypeResolver;

public abstract class AbstractMediaItemAnnotationDao implements MediaItemAnnotationDao {

	private static final ResourceTypeResolver typeResolver = new ResourceTypeResolver();
	
	/* (non-Javadoc)
	 * @see eu.xlime.dao.MediaItemAnnotationDao#findMediaItemEntityAnnotations(java.lang.String)
	 */
	@Override
	public List<EntityAnnotation> findMediaItemEntityAnnotations(String url) {
		if (typeResolver.isNewsArticle(url))
			return findNewsArticleEntityAnnotations(url);
		else if (typeResolver.isMicroPost(url))
			return findMicroPostEntityAnnotations(url);
		else if (typeResolver.isTVProgram(url))
			return ImmutableList.of();// TODO: implement via subtitles, ASR, OCR, annotation of EPG data.
		else throw new RuntimeException("Cannot map url to a known xLiMe media-item type " + url);
	}
	
	protected final VideoSegment newVideoSegment(String tvProgUrl) {
		VideoSegment result = new VideoSegment();
		result.setPartOf(retrieveTVProgramOr(tvProgUrl, emptyTVProgramBean(tvProgUrl)));
		return result;
	}

	/**
	 * If available, return a {@link MediaItemDao}, which will help to produce complete beans when 
	 * annotations embed the annotated MediaItem.
	 * 
	 * @return
	 */
	protected Optional<MediaItemDao> getMediaItemDao() {
		return Optional.absent();
	}

	private final TVProgramBean retrieveTVProgramOr(String tvProgUrl, TVProgramBean defaultVal) {
		Optional<MediaItemDao> optMedItDao = getMediaItemDao();
		if (optMedItDao.isPresent()) {
			Optional<TVProgramBean> optResult = optMedItDao.get().findTVProgram(tvProgUrl);
			return optResult.or(defaultVal);
		} else return defaultVal;
	}
	
	private TVProgramBean emptyTVProgramBean(String tvProgUrl) {
		TVProgramBean result = new TVProgramBean();
		result.setUrl(tvProgUrl);
		return result;
	}

	protected final List<OCRAnnotation> cleanOCRAnnotations(
			List<OCRAnnotation> list) {
		for (OCRAnnotation dirty: list){
			cleanOCRAnnotation(dirty);
		}
		return list;
	}
	
	protected final List<SubtitleSegment> cleanSubTitleSegments(
			List<SubtitleSegment> list) {
		for (SubtitleSegment dirty: list){
			cleanSubtitleSegment(dirty);
		}
		return list;
	}

	private OCRAnnotation cleanOCRAnnotation(OCRAnnotation dirty) {
		Optional<MediaItemDao> optMedItDao = getMediaItemDao();
		if (isEmpty(dirty.getInSegment().getPartOf()) && optMedItDao.isPresent()) {
			TVProgramBean emptyBean = dirty.getInSegment().getPartOf();
			TVProgramBean cleanBean = retrieveTVProgramOr(emptyBean.getUrl(), emptyBean);
			dirty.getInSegment().setPartOf(cleanBean);
			cleanVideoSegment(dirty.getInSegment());
		}
		return dirty;
	}
	
	private SubtitleSegment cleanSubtitleSegment(SubtitleSegment dirty) {
		Optional<MediaItemDao> optMedItDao = getMediaItemDao();
		if (isEmpty(dirty.getPartOf().getPartOf()) && optMedItDao.isPresent()) {
			TVProgramBean emptyBean = dirty.getPartOf().getPartOf();
			TVProgramBean cleanBean = retrieveTVProgramOr(emptyBean.getUrl(), emptyBean);
			dirty.getPartOf().setPartOf(cleanBean);
			cleanVideoSegment(dirty.getPartOf());
		}
		return dirty;
	}

	private boolean isEmpty(TVProgramBean partOf) {
		return partOf.getBroadcastDate() == null || partOf.getTitle() == null;
	}

	protected final VideoSegment cleanVideoSegment(VideoSegment vidSeg) {
		vidSeg.setUrl(calcVideoSegmentUrl(vidSeg));
		vidSeg.setWatchUrl(calcVideoSegmentWatchUrl(vidSeg));
		return vidSeg;
	}
	
	private String calcVideoSegmentUrl(VideoSegment vidSeg) {
		if (vidSeg.getPosition() instanceof ZattooStreamPosition) {
			return String.format("%s/%s", vidSeg.getPartOf().getUrl(), ((ZattooStreamPosition)vidSeg.getPosition()).getValue());
		} else throw new RuntimeException("Cannot coin url for " + vidSeg);
	}

	private String calcVideoSegmentWatchUrl(VideoSegment vidSeg) {
		if (vidSeg.getPosition() instanceof ZattooStreamPosition) {
			TVProgramBean tvProg = vidSeg.getPartOf();
			if (tvProg.getWatchUrl() != null && tvProg.getBroadcastDate() != null && tvProg.getDuration() != null) {
				final long start = tvProg.getBroadcastDate().timestamp.getTime();
				final long end = start + (long)(tvProg.getDuration().getTotalSeconds() * 1000);
				final double streamPosStart = 1073741823.5;
				final long streamPosTimeStamp = 1000L * (long)(streamPosStart + (4.0 * ((ZattooStreamPosition)vidSeg.getPosition()).getValue()));
				final long offset = streamPosTimeStamp - start;
				return String.format("%s/%s/%s/%s", tvProg.getWatchUrl(), start, end, offset);
			} else {
				//cannot calculate watch Url without start and end time
				return null;
			}
		} else throw new RuntimeException("Cannot coin url for " + vidSeg);
	}

	/**
	 * Cleans the list of {@link EntityAnnotation}s by: combining duplicates (merging its positions 
	 * and merging their confidence scores) and sorting the results to showing the annotations with
	 * a higher confidence first. This method could also filter annotations to only show those above
	 * a certain threshold.
	 * 
	 * @param list
	 * @return
	 */
	protected List<EntityAnnotation> cleanEntAnns(List<EntityAnnotation> list) {
		final Map<String, EntityAnnotation> merged = new HashMap<>();
		for (EntityAnnotation unmerged : list) {
			String entUrl = unmerged.getEntity().getUrl();
			EntityAnnotation mergedEA = unmerged;
			if (merged.containsKey(entUrl)) {
				mergedEA = merged.get(entUrl);
				mergedEA.setConfidence(best(mergedEA.getConfidence(), unmerged.getConfidence()));
			} else {
				merged.put(entUrl, unmerged);
			}
		}
		
		Ordering<EntityAnnotation> byConfidence = Ordering.natural().reverse().onResultOf(new Function<EntityAnnotation, Double>() {
			@Override
			public Double apply(EntityAnnotation input) {
				return input.getConfidence();
			}
		});
		
		List<EntityAnnotation> result = new ArrayList<>(merged.values());
		Collections.sort(result, byConfidence);
		
		//TODO: cut-off at threshold?
		return ImmutableList.copyOf(result);
	}

	private double best(double a, double b) {
		return Math.max(a, b);
	}
	
}
