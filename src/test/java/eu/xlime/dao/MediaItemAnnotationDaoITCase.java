package eu.xlime.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import eu.xlime.bean.EntityAnnotation;
import eu.xlime.dao.MediaItemAnnotationDao;

public class MediaItemAnnotationDaoITCase {

	@Test @Ignore("Depends on state of sparql endpoint and may timeout")
	public void testFindMicroPostEntityAnnotations() {
		MediaItemAnnotationDao testObj = new MediaItemAnnotationDao();
		List<EntityAnnotation> anns = testObj.findMicroPostEntityAnnotations("http://vico-research.com/social/056eeb12-6a21-38af-b40c-94fbabe8628f");
		System.out.println("Found micropost annotations " + anns);
		assertTrue(!anns.isEmpty());
		assertEquals(6, anns.size());
	}
	
}
