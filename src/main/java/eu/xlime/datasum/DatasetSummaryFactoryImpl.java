package eu.xlime.datasum;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xlime.Config;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.dao.MongoXLiMeResourceStorer;
import eu.xlime.datasum.bean.DatasetSummary;
import eu.xlime.datasum.bean.HistogramItem;
import eu.xlime.summa.bean.UIEntity;

public class DatasetSummaryFactoryImpl implements DatasetSummaryFactory {

	private static final Logger log = LoggerFactory.getLogger(DatasetSummaryFactoryImpl.class);
	
	private static final String[] knownKeywordFilters = { "Econda Shoes SL", "Econda Shoes ES", "Econda Shoes EN", "Econda Shoes CA", "Econda Shoes DE", "Brexit ES", "Brexit DE", "Brexit EN" };
	
	/* (non-Javadoc)
	 * @see eu.xlime.datasum.DatasetSummaryFactory#createXLiMeSparqlSummary()
	 */
	@Override
	public DatasetSummary createXLiMeSparqlSummary() {
		DatasetSummary result = new DatasetSummary();
		result.setName("xLiMe Sparql endpoint");
		result.setDescription("Private Sparql endpoint, typically containing between one to three months of xLiMe data. Please contact us if you want access to this endpoint.");
		
		SparqlDatasetSummaryDaoImpl dao = new SparqlDatasetSummaryDaoImpl();
		List<String> errors = new ArrayList<String>();
		List<String> msgs = new ArrayList<String>();		
		try {
			result.setActivities(dao.getNumActivities());
		} catch (Exception e) {
			final String msg = "Failed to count activities"; 
			log.error(msg, e);
			errors.add(msg);
		}
		try {
			result.setMicroposts(dao.getNumMicroposts());
		} catch (Exception e) {
			final String msg = "Failed to count microposts"; 
			log.error(msg, e);
			errors.add(msg);
		}
		
		List<HistogramItem> microPostFilters = new ArrayList<HistogramItem>();
		for (String keywordFilter: knownKeywordFilters) {
			HistogramItem it = new HistogramItem();
			it.setItem(keywordFilter);
			try {
				it.setCount(dao.getNumMicropostsbyFilter(keywordFilter));
				microPostFilters.add(it);
			} catch (Exception e) {
				final String msg = "Failed to count microposts with keywordFilter " + keywordFilter; 
				log.error(msg, e);
				errors.add(msg);
			}
		}
		result.setMicroposts_filter(microPostFilters);
		
		try {
			result.setNewsarticles(dao.getNumNewsarticles());
		} catch (Exception e) {
			final String msg = "Failed to count news articles"; 
			log.error(msg, e);
			errors.add(msg);
		}
		try {
			result.setMediaresources(dao.getNumMediaresources());
		} catch (Exception e) {
			final String msg = "Failed to count news media resources"; 
			log.error(msg, e);
			errors.add(msg);
		}
		
		try {
			result.setTriples(dao.getNumTriples());
		} catch (Exception e) {
			final String msg = "Failed to count triples"; 
			log.error(msg, e);
			errors.add(msg);
		}
		
			/*
				result.setEntities(eid.getNumEntities());
				result.setSubjects(eid.getNumSubjects());
				result.setPredicates(eid.getNumPredicates());
				result.setObjects(eid.getNumObjects());
			}*/			

		result.setErrors(errors);
		result.setMessages(msgs);
		result.setSummaryDate(new Date());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.xlime.datasum.DatasetSummaryFactory#createXLiMeMongoSummary()
	 */
	@Override
	public DatasetSummary createXLiMeMongoSummary() {
		DatasetSummary result = new DatasetSummary();
		result.setName("xLiMe Mongo dataset");
		result.setDescription("Private Mongo dataset, used to provide data for front-end services. It typically contains around a week of xLiMe data.");
		List<String> errors = new ArrayList<String>();
		List<String> msgs = new ArrayList<String>();		

		MongoXLiMeResourceStorer resStorer = new MongoXLiMeResourceStorer(new Config().getCfgProps());
		
//		result.setActivities(activities);
		msgs.add("Counting activities not supported yet.");
		
		result.setMicroposts(resStorer.count(MicroPostBean.class));
		result.setNewsarticles(resStorer.count(NewsArticleBean.class));
		result.setMediaresources(resStorer.count(TVProgramBean.class));
		
		result.setEntities(resStorer.count(UIEntity.class));
		
		result.setErrors(errors);
		result.setMessages(msgs);
		result.setSummaryDate(new Date());
		return result;
	}	
}
