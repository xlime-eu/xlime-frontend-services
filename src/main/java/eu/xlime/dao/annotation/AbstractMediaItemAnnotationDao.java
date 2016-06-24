package eu.xlime.dao.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import eu.xlime.bean.EntityAnnotation;
import eu.xlime.dao.MediaItemAnnotationDao;
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
