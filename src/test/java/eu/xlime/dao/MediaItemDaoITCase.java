package eu.xlime.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Optional;

import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.dao.MediaItemDao;

public class MediaItemDaoITCase {

	@Test
	public void testFindNewsArticle() {
		MediaItemDao testObj = new MediaItemDao();
		Optional<NewsArticleBean> bean = testObj.findNewsArticle("http://ijs.si/article/367691732");
		System.out.println("Found newsArticle " + bean);
		assertTrue(bean.isPresent());
	}
	
	@Test
	public void testFindTVProgram() {
		MediaItemDao testObj = new MediaItemDao();
		Optional<TVProgramBean> bean = testObj.findTVProgram("http://zattoo.com/program/111364500");
		System.out.println("Found tv-prog" + bean);
		assertTrue(bean.isPresent());
	}

	@Test
	public void testFindMicroPost() {
		MediaItemDao testObj = new MediaItemDao();
		Optional<MicroPostBean> bean = testObj.findMicroPost("http://vico-research.com/social/c2f2c951-ecea-36fd-bc7d-35f97b736939");
		System.out.println("Found micropost " + bean);
		assertTrue(bean.isPresent());
	}
	
	@Test
	public void testFindLatestMediaItemUrls() {
		MediaItemDao testObj = new MediaItemDao();
		List<String> urls = testObj.findLatestMediaItemUrls(10, 50);
		System.out.println("Found latest " + urls.size() + " urls" + urls);
		assertTrue(!urls.isEmpty());
	}
	
}
