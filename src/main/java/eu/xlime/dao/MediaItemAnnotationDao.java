package eu.xlime.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.AnnotationPosition;
import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.OCRAnnotation;
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

	public Optional<OCRAnnotation> findOCRAnnotation(String uri) {
		log.trace("Finding the OCRAnnotation " + uri);
		// TODO Auto-generated method stub
		return Optional.absent();
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
		Map<String, EntityAnnotation> merged = new HashMap<>();
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
		//TODO: sort by confidence value
		//TODO: cut-off at threshold?
		//TODO: also merge entities by owl:sameas?
		return ImmutableList.copyOf(merged.values());
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
