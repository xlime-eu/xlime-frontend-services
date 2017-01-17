package eu.xlime.datasum;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jersey.repackaged.com.google.common.base.Optional;

import org.mongojack.Aggregation;
import org.mongojack.Aggregation.Pipeline;
import org.mongojack.Aggregation.Pipeline.Stage;
import org.mongojack.AggregationResult;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.mongodb.DBObject;

import eu.xlime.Config;
import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.DatasetInfo;
import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.UIDate;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.dao.MongoXLiMeResourceStorer;
import eu.xlime.datasum.bean.DatasetSummary;
import eu.xlime.datasum.bean.HistogramItem;
import eu.xlime.datasum.bean.ResourceSummary;
import eu.xlime.datasum.bean.TimelineChart;
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
			ResourceSummary mpSum = new ResourceSummary();
			mpSum.setCount(dao.getNumMicroposts());
			result.setMicroposts(mpSum);
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
			ResourceSummary newsSum = new ResourceSummary();
			newsSum.setCount(dao.getNumNewsarticles());
			result.setNewsarticles(newsSum);
		} catch (Exception e) {
			final String msg = "Failed to count news articles"; 
			log.error(msg, e);
			errors.add(msg);
		}
		try {
			ResourceSummary tvSum = new ResourceSummary();
			tvSum.setCount(dao.getNumMediaresources());
			result.setMediaresources(tvSum);
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
		result.setSummaryDate(new UIDate(new Date()));
		return result;
	}

	private final DatasetInfo defaultDatasetInfo = createDefaultDatasetInfo();
	
	/* (non-Javadoc)
	 * @see eu.xlime.datasum.DatasetSummaryFactory#createXLiMeMongoSummary()
	 */
	@Override
	public DatasetSummary createXLiMeMongoSummary() {
		DatasetSummary result = new DatasetSummary();
		List<String> errors = new ArrayList<String>();
		List<String> msgs = new ArrayList<String>();		

		MongoXLiMeResourceStorer resStorer = new MongoXLiMeResourceStorer(new Config().getCfgProps());

		DatasetInfo dsInfo = resStorer.getDBCollection(DatasetInfo.class).findOne();
		if (dsInfo == null) dsInfo = defaultDatasetInfo;
		result.setName(dsInfo.getName());
		result.setDescription(dsInfo.getDescription());
		
//		result.setActivities(activities);
		msgs.add("Counting activities not supported yet.");
		
		ResourceSummary mpSum = createResourceSummary(resStorer, MicroPostBean.class);
		result.setMicroposts(mpSum);
		
		ResourceSummary newsSum = createResourceSummary(resStorer, NewsArticleBean.class);
		result.setNewsarticles(newsSum);
		
		ResourceSummary tvSum = createResourceSummary(resStorer, TVProgramBean.class);
		result.setMediaresources(tvSum);

		ResourceSummary entAnnSum = createResourceSummary(resStorer, EntityAnnotation.class);
		result.setEntityAnnotations(entAnnSum);
		
		ResourceSummary asrSum = createResourceSummary(resStorer, ASRAnnotation.class);
		result.setAsrAnnotations(asrSum);

		ResourceSummary ocrSum = createResourceSummary(resStorer, OCRAnnotation.class);
		result.setOcrAnnotations(ocrSum);

		ResourceSummary subtitSum = createResourceSummary(resStorer, SubtitleSegment.class);
		result.setSubtitles(subtitSum);
		
		result.setEntities(resStorer.count(UIEntity.class));
		
		result.setErrors(errors);
		result.setMessages(msgs);
		result.setSummaryDate(new UIDate(new Date()));
		return result;
	}

	
	private DatasetInfo createDefaultDatasetInfo() {
		DatasetInfo result = new DatasetInfo();
		result.setName("xLiMe Mongo dataset");
		result.setDescription("Private Mongo dataset, used to provide data for front-end services. It typically contains around a week of xLiMe data.");
		result.setUrl("http://xlime.eu/vocab/datasetinfo/default");
		return result;
	}


	@Override
	public List<TimelineChart> retrieveTimelineCharts(String resourceType,
			String metric) {
		MongoXLiMeResourceStorer resStorer = new MongoXLiMeResourceStorer(new Config().getCfgProps());
		
		DBCursor<TimelineChart> cursor = resStorer.getDBCollection(TimelineChart.class).find()
			.in("topic", ImmutableList.of(resourceType))
			.in("metric", ImmutableList.of(metric))
			.sort(DBSort.desc("date"));
		
		return cursor.toArray();
	}

	@Override
	public List<String> retrieveAvailableTimelineResourceTypes() {
		MongoXLiMeResourceStorer resStorer = new MongoXLiMeResourceStorer(new Config().getCfgProps());
		return resStorer.getDBCollection(TimelineChart.class).distinct("topic");
	}

	public List<String> retrieveAvailableMetricsForResourceType(String resourceType) {
		MongoXLiMeResourceStorer resStorer = new MongoXLiMeResourceStorer(new Config().getCfgProps());
		Query q = DBQuery.in("topic", ImmutableList.of(resourceType));
		Pipeline<?> pipeline = Aggregation.match(q).group("metric");

		AggregationResult<TimelineChart> aggResult = resStorer.getDBCollection(TimelineChart.class).aggregate(pipeline, TimelineChart.class);
		List<String> result = new ArrayList<>();
		for (DBObject obj: aggResult.getAggregationOutput().results()) {
			result.add(obj.get("_id").toString());
		}
		log.info("aggResults" + result);
		return ImmutableList.copyOf(result);//toMetrics(aggResult.results());
	}
	
	private <T extends XLiMeResource> ResourceSummary createResourceSummary(
			MongoXLiMeResourceStorer resStorer, Class<T> cls) {
		ResourceSummary mpSum = new ResourceSummary();
		mpSum.setCount(resStorer.count(cls));
		if (mpSum.getCount() > 0) {
			try {
				UIDate mpnd = clean(getDate(getNewest(resStorer, cls, 1).get(0)));
				mpSum.setNewestDate(mpnd);
			} catch (Exception e) {
				log.warn(String.format("Error getting newest date for %s. %s.", cls, e.getLocalizedMessage()));
			}

			try {
				UIDate mpod = clean(getDate(getOldest(resStorer, cls, 1).get(0)));
				mpSum.setOldestDate(mpod);
			} catch (Exception e) {
				log.warn(String.format("Error getting oldest date for %s. %s.", cls, e.getLocalizedMessage()));
			}
		}
		return mpSum;
	}
	
	private <T extends XLiMeResource> List<T> getNewest(MongoXLiMeResourceStorer resStorer, Class<T> miCls, int limit) {
		boolean ascending = true;
		return resStorer.getSortedByDate(miCls, !ascending, limit);
	}

	private UIDate clean(UIDate date) {
		if (date == null) return date;
		date.resetTimeAgo();
		return date;
	}
	
	private <T extends XLiMeResource> UIDate getDate(T res) {
		if (res == null) return null;
		if (res instanceof TVProgramBean) {
			return ((TVProgramBean)res).getBroadcastDate();
		} else if (res instanceof NewsArticleBean) {
			return ((NewsArticleBean)res).getCreated();
		} else if (res instanceof MicroPostBean) {
			return ((MicroPostBean)res).getCreated();
		} else if (res instanceof ASRAnnotation) {
			return ((ASRAnnotation)res).getInSegment().getStartTime();
		} else if (res instanceof OCRAnnotation) {
			return ((OCRAnnotation)res).getInSegment().getStartTime();
		} else if (res instanceof SubtitleSegment) {
			return ((SubtitleSegment)res).getPartOf().getStartTime();
		} else if (res instanceof EntityAnnotation) {
			return new UIDate(((EntityAnnotation)res).getInsertionDate());
		} else throw new IllegalArgumentException("resource" + res);
		
	}
	private <T extends XLiMeResource> List<T> getOldest(MongoXLiMeResourceStorer resStorer, Class<T> miCls, int limit) {
		boolean ascending = true;
		return resStorer.getSortedByDate(miCls, ascending, limit);
	}
}
