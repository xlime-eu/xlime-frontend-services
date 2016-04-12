package eu.xlime.dao;

import com.google.common.base.Optional;

import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.EREvent;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SearchString;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.eventreg.ERDao;
import eu.xlime.summa.UIEntityFactory;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.ResourceTypeResolver;

/**
 * Provides services for retrieving {@link XLiMeResource}s.
 * 
 * @author RDENAUX
 *
 */
public class xLiMeResourceDao {

	private static final MediaItemDao mediaItemDao = new MediaItemDao();
	private static final ERDao eventRegistryDao = new ERDao();
	private static final MediaItemAnnotationDao annotationDao = new MediaItemAnnotationDao();
	private static final ResourceTypeResolver typeResolver = new ResourceTypeResolver();
	
	/**
	 * Retrieves an {@link XLiMeResource} based on its given <code>uri</code>.
	 * 
	 * @param uri
	 * @return
	 */
	public Optional<? extends XLiMeResource> retrieve(String uri) {
		return retrieve(typeResolver.resolveType(uri), uri);
	}
	
	/**
	 * Retrieves an {@link XLiMeResource} based on its given <code>uri</code> and 
	 * resource type.
	 * 
	 * @param resType the resource type of the given <code>uri</code>
	 * @param uri the identifier for the {@link XLiMeResource}
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends XLiMeResource> Optional<T> retrieve(Class<T> resType, String uri) {
		if (resType == null) throw new NullPointerException("Resource type is mandatory");
		if (NewsArticleBean.class.equals(resType)) 
			return (Optional<T>)mediaItemDao.findNewsArticle(uri);
		else if (MicroPostBean.class.equals(resType))
			return (Optional<T>)mediaItemDao.findMicroPost(uri);
		else if (TVProgramBean.class.equals(resType)) 
			return (Optional<T>)mediaItemDao.findTVProgram(uri);
		else if (UIEntity.class.equals(resType)) 
			return (Optional<T>)Optional.of(UIEntityFactory.instance.retrieveFromUri(uri));
		else if (EREvent.class.equals(resType)) 
			return (Optional<T>)eventRegistryDao.retrieveEvent(uri);
		else if (ASRAnnotation.class.equals(resType)) 
			return (Optional<T>)annotationDao.findASRAnnotation(uri);
		else if (OCRAnnotation.class.equals(resType)) 
			return (Optional<T>)annotationDao.findOCRAnnotation(uri);
		else if (VideoSegment.class.equals(resType)) 
			return (Optional<T>)mediaItemDao.findVideoSegment(uri);
		else if (SearchString.class.equals(resType)) 
			return (Optional<T>)Optional.of(new SearchStringFactory().fromUri(uri));
		else throw new RuntimeException("Could not determine xLiMe Resource type for " + uri);
	}
}
