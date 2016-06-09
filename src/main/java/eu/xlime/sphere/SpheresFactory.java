package eu.xlime.sphere;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;

import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.SearchString;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.UrlLabel;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.dao.MediaItemAnnotationDao;
import eu.xlime.dao.MediaItemDao;
import eu.xlime.dao.MediaItemDaoImpl;
import eu.xlime.dao.xLiMeResourceDao;
import eu.xlime.search.SearchItemDao;
import eu.xlime.sphere.bean.Recommendation;
import eu.xlime.sphere.bean.Spheres;
import eu.xlime.summa.UIEntityFactory;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.ResourceTypeResolver;
import eu.xlime.util.score.ScoreFactory;
import eu.xlime.util.score.ScoredSet;
import eu.xlime.util.score.ScoredSetImpl;

/**
 * Implements the building of {@link Spheres} based on an initial context.
 * 
 * @author RDENAUX
 *
 */
public class SpheresFactory {

	private static final Logger log = LoggerFactory.getLogger(SpheresFactory.class);
	private static final ResourceTypeResolver typeResolver = new ResourceTypeResolver();
	private static final xLiMeResourceDao resourceDao = new xLiMeResourceDao();
	private static final MediaItemDao mediaItemDao = new MediaItemDaoImpl();
	private static final MediaItemAnnotationDao mediaItemAnnotationDao = new MediaItemAnnotationDao();
	private static final SearchItemDao searcher = new SearchItemDao();
	private static final UIEntityFactory uiEntityFactory = UIEntityFactory.instance;
	private static final ScoreFactory scoref = ScoreFactory.instance;
		
	public Spheres buildSpheres(List<String> contextUrls) {
		Spheres result = new Spheres();
		List<XLiMeResource> context = resolveContextUrls(contextUrls);
		result.setInner(asTopRecommendations(context));
		List<Recommendation> recs = calcRecs(context);
		if (recs.size() == 0) return result;
		if (recs.size() > 20) {
			result.setInter(recs.subList(0, 8));
			result.setOuter(recs.subList(8, 20));
		} else if (recs.size() > 15) {
			result.setInter(recs.subList(0, 5));
			result.setOuter(recs.subList(5, 15));
		} else {
			result.setInter(recs.subList(0, Math.min(5, recs.size())));
			if (recs.size() > 5) {
				result.setOuter(recs.subList(5, Math.min(15, recs.size())));
			} else result.setOuter(ImmutableList.<Recommendation>of());
		}
		return result;
	}


	private <T extends XLiMeResource> List<Recommendation> mapToRecommendation(
			ScoredSet<T> recs) {
		return mapToRecommendation(recs, recs.asList());
	}

	private <T extends XLiMeResource> List<Recommendation> mapToRecommendation(
			ScoredSet<T> recs, List<T> ress) {
		List<Recommendation> result = new ArrayList<>();
		for (T res: ress) {
			result.add(asRec(recs, res));
		}
		return result;
	}

	private List<Recommendation> calcRecs(List<XLiMeResource> context) {
		List<Recommendation> entities = mapToRecommendation(calcRecEntities(context));
		List<Recommendation> mediaItems = mapToRecommendation(calcRecMediaItems(context));
		log.debug(String.format("Weaving %s entities and %s media-items", entities.size(), mediaItems.size()));
		return weave(entities, mediaItems);
	}

	private List<Recommendation> weave(List<Recommendation>... resLists) {
		List<Recommendation> result = new ArrayList<Recommendation>();
		int maxSize = 0;
		int total = 0;
		for (List<?> list: resLists) {
			maxSize = Math.max(maxSize, list.size());
			total = total + list.size();
		}
		for (int i = 0; i < maxSize; i++) {
			for (List<Recommendation> list: resLists) {
				if (list.size() > i) {
					result.add(list.get(i));
				}
			}
		}
		return result;
	}

	private Recommendation asRec(XLiMeResource xLiMeResource) {
		Recommendation result = new Recommendation();
		result.setConfidence(1.0);
		result.setRecommended(xLiMeResource);
		return result;
	}

	private <T extends XLiMeResource> Recommendation asRec(ScoredSet<T> scored, T xLiMeResource) {
		assert(scored.unscored().contains(xLiMeResource));
		Recommendation result = new Recommendation();
		result.setConfidence(scored.getScore(xLiMeResource).getValue());
		result.setRecommended(xLiMeResource);
		return result;
	}
	
	private ScoredSet<MediaItem> calcRecMediaItems(List<XLiMeResource> context) {
		final ScoredSet.Builder<String> medItUrls = ScoredSetImpl.builder();
		for (XLiMeResource res: context) {
			if (isMediaItem(res)) {
				// TODO: use Adityia's recommender for the supported media items
			} else if (res instanceof SearchString) {
				String keywords = ((SearchString)res).getValue();
				String justif = String.format("Matches search '%s'", keywords);
				ScoredSet<String> miUrls = searcher.findMediaItemUrlsByText(keywords);
				medItUrls.addAll(miUrls);
			} else if (res instanceof UIEntity) {
				String entUrl = ((UIEntity) res).getUrl();
				ScoredSet<String> matches = searcher.findMediaItemUrlsByKBEntity(entUrl); 
				medItUrls.addAll(matches);
			} else {
				// TODO: match other types of 
			}
		}
		return asMediaItems(medItUrls.build()); 
	}

	private ScoredSet<MediaItem> asMediaItems(ScoredSet<String> medItUrls) {
		ScoredSet.Builder<MediaItem> builder = ScoredSetImpl.builder();
		List<MediaItem> mis = mediaItemDao.findMediaItems(medItUrls.asList());
		for (MediaItem mit: mis) {
			builder.add(mit, medItUrls.getScore(mit.getUrl()));
		}
		return builder.build();
	}

	private ScoredSet<UIEntity> calcRecEntities(List<XLiMeResource> context) {
		final ScoredSet.Builder<UIEntity> builder = ScoredSetImpl.builder();
		//TODO: much better to only search for entUris, only convert the topN to UIEntity 
		for (XLiMeResource res: context) {
			if (isMediaItem(res)) {
				MediaItem medIt = (MediaItem) res;
				List<EntityAnnotation> entAnns = mediaItemAnnotationDao.findMediaItemEntityAnnotations(medIt.getUrl());
				log.debug(String.format("Found %s entity annotations for %s", entAnns.size(), medIt.getUrl()));
				for (EntityAnnotation entAnn: entAnns) {
					builder.add(entAnn.getEntity(),
							scoref.newScore(entAnn.getConfidence(), "Mentioned in media item in context"));
				}
			} else if (res instanceof SearchString){
				//TODO: can we use http://km.aifb.kit.edu/services/xlid-lexica-ui/ instead, this would give us greater control and info on confidence for the scores
				String keywords = ((SearchString) res).getValue();
				String justif = String.format("Matches search '%s'", keywords);
				List<UrlLabel> urlLabels = searcher.autoCompleteEntities(keywords);
				for (UrlLabel ul : urlLabels) {
					builder.add(uiEntityFactory.retrieveFromUri(ul.getUrl()),
							scoref.newScore(1.0, justif));
				}
			} else {
				// TODO: recommend other entities for KBEntities?
				// other resources -> entities
			}
		}
		return builder.build();
	}

	/**
	 * Flattens the input collection (which has more information) into an output 
	 * sorted collection. 
	 * @param ents
	 * @return
	 */
	private <T> List<T> flatten(final Multiset<T> ents) {
		List<T> result = new ArrayList<>(ents.elementSet());
		Ordering<T> ordByFreq = Ordering.natural().reverse().onResultOf(new Function<T, Integer>() {

			@Override
			public Integer apply(T input) {
				return ents.count(input);
			}
		}); 
		Collections.sort(result, ordByFreq);
		return result;
	}
	
	private boolean isMediaItem(XLiMeResource res) {
		return (res instanceof TVProgramBean) || (res instanceof NewsArticleBean) || (res instanceof MicroPostBean);
	}

	private List<Recommendation> calcOuterSphere(List<XLiMeResource> context) {
		// TODO implement
		return mockOuterSphere();
	}

	private List<Recommendation> calcInterSphere(List<XLiMeResource> context) {
		// TODO implement
		return mockInterSphere();
	}

	private List<Recommendation> mockInterSphere() {
		List<String> recentMedIts = mediaItemDao.findLatestMediaItemUrls(60, 3);
		ImmutableList<String> uris = ImmutableList.<String>builder()
				.add("http://dbpedia.org/resource/Berlin") //KBEntity 
				.add("http://dbpedia.org/resource/Amsterdam") //TVProg, Soc-Med, News art...
				.addAll(recentMedIts)
				.build();
		return mockSphere(uris, 0.7, 0.9);
	}

	private List<Recommendation> mockSphere(ImmutableList<String> uris, double minConf, double maxConf) {
		List<Recommendation> result = new ArrayList<>();
		Random rand = new Random();
		for (String uri: uris) {
			Recommendation rec = new Recommendation();
			double randConf = minConf + (rand.nextDouble() * (maxConf - minConf));
			rec.setConfidence(randConf);
			Optional<? extends XLiMeResource> res = resourceDao.retrieve(uri);
			if (res.isPresent()) {
				rec.setRecommended(res.get());
				result.add(rec);
			}
		}
		return result;
	}

	private List<Recommendation> mockOuterSphere() {
		List<String> recentMedIts = mediaItemDao.findLatestMediaItemUrls(8, 3);
		ImmutableList<String> uris = ImmutableList.<String>builder()
				.add("http://dbpedia.org/resource/Albert_Einstein") //KBEntity 
				.add("http://dbpedia.org/resource/Nike")
				.add("http://dbpedia.org/resource/Adidas")
				.addAll(recentMedIts) //TVProg, Soc-Med, News art...
				.build(); 
		return mockSphere(uris, 0.4, 0.7);
	}
	
	private List<Recommendation> asTopRecommendations(
			List<XLiMeResource> context) {
		List<Recommendation> result = new ArrayList<>();
		for (XLiMeResource res: context) {
			result.add(asRec(res, 1.0));
		}
		return result;
	}

	private Recommendation asRec(XLiMeResource res, double d) {
		Recommendation result = new Recommendation();
		result.setRecommended(res);
		result.setConfidence(d);
		return result;
	}

	/**
	 * Converts a list of context URIs into a list of corresponding {@link XLiMeResource}s.
	 * 
	 * @param contextUrls
	 * @return
	 */
	private List<XLiMeResource> resolveContextUrls(List<String> contextUrls) {
		List<XLiMeResource> result = new ArrayList<>();
		for (String uri: contextUrls) {
			try {
				Optional<? extends XLiMeResource> optRes = resourceDao.retrieve(uri);
				if (optRes.isPresent()) result.add(optRes.get());
			} catch (Exception e) {
				if (log.isDebugEnabled()) 
					log.error("Error retrieving xLiMeResource from " + uri, e);
			}
		}
		return result;
	}
}
