package eu.xlime.sphere;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import eu.xlime.bean.XLiMeResource;
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

	private static final ResourceTypeResolver typeResolver = new ResourceTypeResolver();
		
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
		return ImmutableList.of();
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
		//TODO: implement this method using typeResolver and the various daos 
		return ImmutableList.of();
	}
}
