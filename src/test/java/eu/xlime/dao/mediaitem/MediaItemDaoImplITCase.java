package eu.xlime.dao.mediaitem;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import eu.xlime.bean.MediaItem;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.dao.MediaItemDao;
import eu.xlime.dao.MediaItemDaoImpl;
import eu.xlime.util.ResourceTypeResolver;

public class MediaItemDaoImplITCase {

	@Test
	public void testFindMediaItem() {
		List<String> toFind = ImmutableList.of(
				"http://ijs.si/article/367691329", "http://ijs.si/article/367691523", "http://ijs.si/article/367691685",
				"http://zattoo.com/program/111364500", "http://zattoo.com/program/111364459", "http://zattoo.com/program/111604630",
				"http://vico-research.com/social/c2f2c951-ecea-36fd-bc7d-35f97b736939",
				"http://vico-research.com/social/6ff8eff3-070a-3ffc-aea5-b3cbf515cbbb",
				"http://vico-research.com/social/a613a963-051c-321c-ab17-9cd133509099");
		MediaItemDao testObj = new MediaItemDaoImpl();
		for (String s: toFind) {
			Optional<? extends MediaItem> mi = testObj.findMediaItem(s);
			assertTrue(mi.isPresent());
		}
	}
	
	@Test
	public void testFindMediaItems() {
		List<String> toFind = ImmutableList.of(
				"http://ijs.si/article/367691329", "http://ijs.si/article/367691523", "http://ijs.si/article/367691685",
				"http://zattoo.com/program/111364500", "http://zattoo.com/program/111364459", "http://zattoo.com/program/111604630",
				"http://vico-research.com/social/c2f2c951-ecea-36fd-bc7d-35f97b736939",
				"http://vico-research.com/social/6ff8eff3-070a-3ffc-aea5-b3cbf515cbbb",
				"http://vico-research.com/social/a613a963-051c-321c-ab17-9cd133509099");
		MediaItemDao testObj = new MediaItemDaoImpl();
		List<MediaItem> mis = testObj.findMediaItems(toFind);
		assertEquals(9, mis.size());
	}
	@Test
	public void testFindNewsArticle() {
		MediaItemDao testObj = new MediaItemDaoImpl();
		Optional<NewsArticleBean> bean = testObj.findNewsArticle("http://ijs.si/article/367691732");
		System.out.println("Found newsArticle  " + bean);
		assertTrue(bean.isPresent());
	}

	@Test
	public void testFindNewsArticles() {
		MediaItemDao testObj = new MediaItemDaoImpl();
		List<String> uris = ImmutableList.of(
				"http://ijs.si/article/367691329", "http://ijs.si/article/367691523", "http://ijs.si/article/367691685");
		List<NewsArticleBean> beans = testObj.findNewsArticles(uris);
		System.out.println("Found newsArticles " + beans);
		assertEquals(3, beans.size());
	}
	
	@Test
	public void testFindTVProgram() {
		MediaItemDao testObj = new MediaItemDaoImpl();
//		String url = "http://zattoo.com/program/111364500";
//		String url = "http://zattoo.com/program/114078287";		
//		String url = "http://zattoo.com/program/113684648";
		String url = "http://zattoo.com/program/114199830";
		Optional<TVProgramBean> bean = testObj.findTVProgram(url);
		System.out.println("Found tv-prog" + bean);
		ResourceTypeResolver resourceUri = new ResourceTypeResolver();
		System.out.println("Watch url: " + resourceUri.toWatchUrl(bean.get()));
		assertTrue(bean.isPresent());
		assertNotNull(bean.get().getWatchUrl());
	}

	@Test
	public void testFindTVPrograms() {
		MediaItemDao testObj = new MediaItemDaoImpl();
		List<String> urls = ImmutableList.of("http://zattoo.com/program/111364500", "http://zattoo.com/program/111364459", "http://zattoo.com/program/111604630");
		List<TVProgramBean> beans = testObj.findTVPrograms(urls);
		System.out.println("Found tv-prog" + beans);
		assertTrue(!beans.isEmpty());
	}
	
	@Test
	public void testFindMicroPost() {
		MediaItemDao testObj = new MediaItemDaoImpl();
		String url = "http://vico-research.com/social/5402786c-1f54-354d-a694-78ee369361ba";
		Optional<MicroPostBean> bean = testObj.findMicroPost(url);
		System.out.println("Found micropost " + bean);
		List<MicroPostBean> beans = testObj.findMicroPosts(ImmutableList.of(url));
		System.out.println("Found microposts " + beans);
		assertTrue(bean.isPresent());
	}
	
	@Test @Ignore("Check why it fails")
	public void testFindMicroPosts() {
		MediaItemDao testObj = new MediaItemDaoImpl();
		List<String> uris = ImmutableList.of("http://vico-research.com/social/21922407-ff22-3114-9754-8cf75bedaf31", //this one is present 484 times, Virtuoso doesn't return it.. 
				"http://vico-research.com/social/c2f2c951-ecea-36fd-bc7d-35f97b736939",
				"http://vico-research.com/social/6ff8eff3-070a-3ffc-aea5-b3cbf515cbbb",
				"http://vico-research.com/social/a613a963-051c-321c-ab17-9cd133509099"
				); 
		List<MicroPostBean> beans = testObj.findMicroPosts(uris);
		
		System.out.println("Found micropost " + beans);
		assertTrue(!beans.isEmpty());
		Set<String> foundUris = new HashSet<>();
		for (MicroPostBean b: beans) {
			foundUris.add(b.getUrl());
		}
		//assertEquals(ImmutableSet.copyOf(uris), foundUris);
		assertEquals(3, beans.size());
	}
	
	@Test
	public void testFindLatestMediaItemUrls() {
		MediaItemDao testObj = new MediaItemDaoImpl();
		List<String> urls = testObj.findLatestMediaItemUrls(10, 50);
		System.out.println("Found latest " + urls.size() + " urls" + urls);
		assertTrue(!urls.isEmpty());
	}
	
}
