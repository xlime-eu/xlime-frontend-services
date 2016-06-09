package eu.xlime.sphere;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.xlime.sphere.bean.Spheres;

public class SpheresFactoryITCase {

	@Test
	public void testBuildSpheres() {
		SpheresFactory testObj = new SpheresFactory();
		List<String> context = ImmutableList.of("http://ijs.si/article/367691329",
				"http://zattoo.com/program/111364500", 
				"http://vico-research.com/social/c2f2c951-ecea-36fd-bc7d-35f97b736939",
				"http://dbpedia.org/resource/Berlin",
				"http://xlime.eu/vocab/search?q=Refugee"
				);
		long start = System.currentTimeMillis();
		Spheres s = testObj.buildSpheres(context);
		long end = System.currentTimeMillis();
		long time = (end - start);
		System.out.println("Found spheres in (" + time + "ms.) : " + s);
		assertNotNull(s);
	}
}
