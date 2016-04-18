package eu.xlime.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.AnnotationPosition;
import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;
import eu.xlime.sparql.SparqlQueryFactory;
import eu.xlime.summa.UIEntityFactory;
import eu.xlime.util.KBEntityMapper;
import eu.xlime.util.ResourceTypeResolver;

public class MediaItemAnnotationDao {

	private static final Logger log = LoggerFactory.getLogger(MediaItemAnnotationDao.class);
	
	private static final SparqlQueryFactory qFactory = new SparqlQueryFactory();
	private static final ResourceTypeResolver typeResolver = new ResourceTypeResolver();
	private static final KBEntityMapper kbEntityMapper = new KBEntityMapper();	

	public List<EntityAnnotation> findMediaItemEntityAnnotations(String url) {
		if (typeResolver.isNewsArticle(url))
			return findNewsArticleEntityAnnotations(url);
		else if (typeResolver.isMicroPost(url))
			return findMicroPostEntityAnnotations(url);
		else if (typeResolver.isTVProgram(url))
//			return mediaItemAnnotationDao.findTVProgram(url);
			throw new RuntimeException("tv programmes not supported yet");
		else throw new RuntimeException("Cannot map url to a known xLiMe media-item type " + url);
	}
	
	public List<EntityAnnotation> findMicroPostEntityAnnotations(final String url) {
		log.trace("Finding entity annotations for micropost " + url);
		final SparqlClient sparqler = getXliMeSparqlClient();
		String query = qFactory.microPostEntityAnnotations(url);
		Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
		
		return toEntityAnnotations(result, url);
	}
	
	private SparqlClient getXliMeSparqlClient() {
		return new SparqlClientFactory().getXliMeSparqlClient();
	}

	public List<EntityAnnotation> findNewsArticleEntityAnnotations(final String url) {
		log.trace("Finding entity annotations for newsarticle " + url);
		final SparqlClient sparqler = getXliMeSparqlClient();
		String query = qFactory.newsArticleEntityAnnotations(url);
		Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
		
		return toEntityAnnotations(result, url);
	}
	
	public Optional<ASRAnnotation> findASRAnnotation(String uri) {
		log.trace("Finding the ASRAnnotation " + uri);
		// TODO implement
		return Optional.absent();
	}

	public List<OCRAnnotation> findOCRAnnotationsFor(final TVProgramBean mediaResource) {
		log.trace("Finding the OCRAnnotations for " + mediaResource);
		final SparqlClient sparqler = getXliMeSparqlClient();
		String query = qFactory.mediaResourceOCRAnnotations(mediaResource.getUrl());
		Map<String, Map<String, String>> result = sparqler.executeSPARQLQuery(query);
		
		return toMediaResourceOCRAnnotations(result, mediaResource);
	}
	
	public Optional<OCRAnnotation> findOCRAnnotation(String ocrAnnotUri) {
		log.trace("Finding the OCRAnnotation " + ocrAnnotUri);
		/* TODO: since OCR annotations do not have uris in sparql enpoint:
		 * 1. map to mediaResource uri via naming convention, 
		 * 2. find mediaResource via uri (needs MediaItemDao), 
		 * 3. find all OCR annotations via #findOCRAnnotationsFor and 
		 * 4. return one that matches, if any... 
		 */
		return Optional.absent();
	}
	
	private List<OCRAnnotation> toMediaResourceOCRAnnotations(
			Map<String, Map<String, String>> resultSet, TVProgramBean tvProg) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("No ocr annotations for " + tvProg.getUrl());
			return ImmutableList.of();
		}		
		log.debug("Creating OCRAnnotation from resultset with " + resultSet.size() + " tuples.");
		List<OCRAnnotation> result = new ArrayList<>();
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			Optional<OCRAnnotation> optAnn = toOCRAnnotation(tuple, tvProg);
			if (optAnn.isPresent()) result.add(optAnn.get());
		}		
		return result;
	}

	final Optional<OCRAnnotation> toOCRAnnotation(Map<String, String> tuple,
			TVProgramBean tvProg) {
		OCRAnnotation result = new OCRAnnotation();
		if (tuple.containsKey("ocr") && tuple.containsKey("vidTrack")) {
			OCRContent ocrContent = new OCRContent(tuple.get("ocr"));
			result.setInSegment(toVideoSegment(tvProg, ocrContent.timestamp));
			result.setRecognizedText(ocrContent.recognizedText);
		} else {
			log.warn("No OCR content found for " + tvProg.getUrl());
			return Optional.absent();
		}
		return Optional.of(result);
	}
	
	private VideoSegment toVideoSegment(TVProgramBean tvProg, double timestamp) {
		VideoSegment result = new VideoSegment();
		result.setPartOf(tvProg);
		return result;
	}

	public static class OCRContent {
		final String literal;
		final double timestamp;
		final String recognizedText;
		
		public OCRContent(String literalOCRContent) {
			literal = literalOCRContent;
			timestamp = extractTimeStamp(literalOCRContent);
			recognizedText = extractRecognizedText(literalOCRContent);
		}
		
		private String extractRecognizedText(String literalOCRContent) {
			int splitIndex = literalOCRContent.indexOf(',');
			return literalOCRContent.substring(splitIndex + 1);
		}

		private double extractTimeStamp(String literalOCRContent) {
			int splitIndex = literalOCRContent.indexOf(',');
			if (splitIndex <= 0) throw new RuntimeException("Illegal format of OCR content. "
					+ "Expecting :\n\t<timestamp> ', ' <recognizedText>\n\t but found: \n\t " + literalOCRContent);
			return Double.parseDouble(literalOCRContent.substring(0, splitIndex));
		}

	}

	private List<EntityAnnotation> toEntityAnnotations(Map<String, Map<String, String>> resultSet,
			String url) {
		if (resultSet == null || resultSet.keySet().isEmpty()) {
			log.debug("No entities for " + url);
			return ImmutableList.of();
		}
		log.debug("Creating EntityAnnotation from resultset with " + resultSet.size() + " tuples.");
		List<EntityAnnotation> result = new ArrayList<>();
		for (String id: resultSet.keySet()) {
			Map<String, String> tuple = resultSet.get(id);
			Optional<EntityAnnotation> optEntAnn = toEntityAnnotation(tuple);
			if (optEntAnn.isPresent()) result.add(optEntAnn.get());
		}
		return cleanEntAnns(result);
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
	private List<EntityAnnotation> cleanEntAnns(List<EntityAnnotation> list) {
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

	private Optional<EntityAnnotation> toEntityAnnotation(Map<String, String> tuple) {
		EntityAnnotation ea = new EntityAnnotation();
		try {
			if (tuple.containsKey("ent")) {
				String entUrl = tuple.get("ent");
				Optional<String> canonEntUrl = kbEntityMapper.toCanonicalEntityUrl(entUrl);
				if (!canonEntUrl.isPresent()) {
					log.debug("No canonical entity found for " + entUrl + " not converting to EntityAnnotation");
					return Optional.absent();
				} else {
					ea.setEntity(UIEntityFactory.instance.retrieveFromUri(canonEntUrl.get()));
				}
			} else return Optional.absent(); //having an entity is mandatory
			if (tuple.containsKey("confidence")) {
				ea.setConfidence(Double.parseDouble(tuple.get("confidence")));
			}
		} catch (Exception e) {
			log.debug("Error reading EntityAnnotation from tuple" + e);
			return Optional.absent();
		}
		return Optional.of(ea);
	}

	private Optional<AnnotationPosition> toAnnPosition(Map<String, String> tuple) {
		AnnotationPosition pos = new AnnotationPosition();
		try {
			if (tuple.containsKey("start")) {
				pos.setStart(Long.parseLong(tuple.get("start")));
			}
			if (tuple.containsKey("end")) {
				pos.setStart(Long.parseLong(tuple.get("end")));
			}
		} catch (NumberFormatException ne) {
			log.debug("Error parsing annotation position", ne);
			return Optional.absent();
		}
		return Optional.of(pos);
	}


}
