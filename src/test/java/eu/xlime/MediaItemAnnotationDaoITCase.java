package eu.xlime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import eu.xlime.bean.EntityAnnotation;

public class MediaItemAnnotationDaoITCase {

	@Test
	public void testFindMicroPostEntityAnnotations() {
		MediaItemAnnotationDao testObj = new MediaItemAnnotationDao();
		List<EntityAnnotation> anns = testObj.findMicroPostEntityAnnotations("http://vico-research.com/social/056eeb12-6a21-38af-b40c-94fbabe8628f");
		System.out.println("Found micropost annotations " + anns);
		assertTrue(!anns.isEmpty());
		assertEquals(5, anns.size());
	}
	
}
