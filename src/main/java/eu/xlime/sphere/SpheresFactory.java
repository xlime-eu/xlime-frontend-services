package eu.xlime.sphere;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import eu.xlime.bean.XLiMeResource;
import eu.xlime.dao.xLiMeResourceDao;
import eu.xlime.sphere.bean.Recommendation;
import eu.xlime.sphere.bean.Spheres;
import eu.xlime.util.ResourceTypeResolver;

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
		
	public Spheres buildSpheres(List<String> contextUrls) {
		Spheres result = new Spheres();
		List<XLiMeResource> context = resolveContextUrls(contextUrls);
		result.setInner(asTopRecommendations(context));
		result.setInter(calcInterSphere(context));
		result.setOuter(calcOuterSphere(context));
		return result;
	}

	private List<Recommendation> calcOuterSphere(List<XLiMeResource> context) {
		// TODO implement
		return ImmutableList.of();
	}

	private List<Recommendation> calcInterSphere(List<XLiMeResource> context) {
		// TODO implement
		return mockInterSphere();
	}

	private List<Recommendation> mockInterSphere() {
		List<Recommendation> result = new ArrayList<>();
		ImmutableList<String> uris = ImmutableList.of(
				"http://dbpedia.org/resource/Berlin", //KBEntity 
				"http://"); //TVProg, Soc-Med, News art...
		Recommendation r1 = new Recommendation();
		r1.setConfidence(0.9);
//		r1.setRecommended(resourceDao.retrieve("")); //TODO: implement
		return result;
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
