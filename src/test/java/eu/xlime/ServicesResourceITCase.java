package eu.xlime;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Optional;

import eu.xlime.bean.MediaItem;

public class ServicesResourceITCase {

	@Test
	public void testFindMediaItem_news() {
		ServicesResource testObj = new ServicesResource();
		Optional<? extends MediaItem> bean = testObj.findMediaItem("http://ijs.si/article/367691732");
		System.out.println(bean);
		assertNotNull(bean);
	}
	
	@Test
	public void testFindMediaItem_socmed() {
		ServicesResource testObj = new ServicesResource();
		Optional<? extends MediaItem> bean = testObj.findMediaItem("http://vico-research.com/social/056eeb12-6a21-38af-b40c-94fbabe8628f");
		System.out.println(bean);
		assertNotNull(bean);
	}

}
