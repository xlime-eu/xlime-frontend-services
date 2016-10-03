package eu.xlime.dao.annotation;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.EntityAnnotation;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.dao.MediaItemDao;
import eu.xlime.dao.MongoXLiMeResourceStorer;
import eu.xlime.dao.mediaitem.MongoMediaItemDao;
import eu.xlime.mongo.DBCollectionProvider;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.ResourceTypeResolver;
import eu.xlime.util.score.Score;
import eu.xlime.util.score.ScoreFactory;
import eu.xlime.util.score.ScoredSet;
import eu.xlime.util.score.ScoredSetImpl;

/**
 * 
 * @author rdenaux
 *
 */
public class MongoMediaItemAnnotationDao extends AbstractMediaItemAnnotationDao {

	private static final Logger log = LoggerFactory.getLogger(MongoMediaItemAnnotationDao.class);
	
	private final MongoXLiMeResourceStorer mongoStorer;
	private final MongoMediaItemDao miDao;
	private final DBCollectionProvider collectionProvider;
	private static final ResourceTypeResolver typeResolver = new ResourceTypeResolver();
	private static final ScoreFactory scoref = ScoreFactory.instance;

	private final int defaultMax = 30;
	
	public MongoMediaItemAnnotationDao(Properties props) {
		collectionProvider = new DBCollectionProvider(props);
		mongoStorer = new MongoXLiMeResourceStorer(collectionProvider);
		miDao = new MongoMediaItemDao(props);
	}
	
	@Override
	protected Optional<MediaItemDao> getMediaItemDao() {
		return Optional.<MediaItemDao>of(miDao);
	}

	public List<EntityAnnotation> findEntityAnnotations(int limit) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find();
		log.debug(String.format("Found %s EntAnns", cursor.count()));
		return cursor.toArray(limit);
	}
	
	@Override
	public List<EntityAnnotation> findMicroPostEntityAnnotations(
			String microPostUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(microPostUrl));
		log.debug(String.format("Found %s EntAnns", cursor.count()));
		return cursor.toArray(defaultMax);
	}

	@Override
	public List<EntityAnnotation> findNewsArticleEntityAnnotations(
			String newsArticleUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(newsArticleUrl));
		log.debug(String.format("Found %s EntAnns", cursor.count()));
		return cursor.toArray(defaultMax);
	}

	@Override
	public List<EntityAnnotation> findSubtitleTrackEntityAnnotations(
			String subtitleTrackUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(subtitleTrackUrl));
		log.debug(String.format("Found %s EntAnns", cursor.count()));
		return cursor.toArray(defaultMax);
	}

	@Override
	public List<EntityAnnotation> findEntityAnnotationsFor(UIEntity kbEntity) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("entity._id", ImmutableList.of(kbEntity.getUrl()));
		log.debug(String.format("Found %s EntAnns", cursor.count()));
		return cursor.toArray(defaultMax);
	}

	@Override
	public ScoredSet<String> findMediaItemUrlsByKBEntity(String entityUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("entity._id", ImmutableList.of(entityUrl)).sort(DBSort.desc("confidence"));
		log.debug(String.format("Found %s EntAnns", cursor.count()));
		return toMediaItemUrlScoredSet(cursor);
	}

	private ScoredSet<String> toMediaItemUrlScoredSet(
			DBCursor<EntityAnnotation> cursor) {
		ScoredSet.Builder<String> builder = ScoredSetImpl.builder();
		for (EntityAnnotation ea: cursor.toArray(defaultMax)) {
			Optional<String> miUrl = mapAnnotatedResourceUrlToMediaItemUrl(ea.getResourceUrl());
			if (miUrl.isPresent()) {
				builder.add(miUrl.get(), toScore(ea));
			}
		}
		return builder.build();
	}

	private Optional<String> mapAnnotatedResourceUrlToMediaItemUrl(
			String resourceUrl) {
		if (typeResolver.isMediaItem(resourceUrl)) return Optional.of(resourceUrl);
		try {
			if (typeResolver.isSubtitleTrack(resourceUrl)) {
				return typeResolver.subtitleTrackUrlAsTVProgUrl(resourceUrl);
			}
		} catch (Exception e) {
			log.error(String.format("Error mapping annotated resource %s to a mediaItem url", resourceUrl));
		}
		return Optional.absent();
	}

	private Score toScore(EntityAnnotation ea) {
		return scoref.newScore(ea.getConfidence(), "Entity annotation on " + ea.getResourceUrl());
	}

	@Override
	public Optional<ASRAnnotation> findASRAnnotation(String mediaItemUri) {
		log.warn("findASRAnnotation() not implemented yet"); 
		return Optional.absent();
	}

	@Override
	public List<ASRAnnotation> findAllASRAnnotations(int limit) {
		DBCursor<ASRAnnotation> cursor = mongoStorer.getDBCollection(ASRAnnotation.class).find();
		log.debug(String.format("Found %s ASRAnnotations", cursor.count()));
		if (cursor.count() > limit) 
			return cleanASRAnnotations(cursor.skip(cursor.count() - limit).toArray(limit));
		return cleanASRAnnotations(cursor.toArray(limit));
	}

	@Override
	public List<ASRAnnotation> findASRAnnotationsByText(String textQuery) {
		DBObject textQ = new BasicDBObject(
			    "$text", new BasicDBObject("$search", textQuery)
				);
		DBObject projection = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				);
		DBObject sorting = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				); 
		long start = System.currentTimeMillis();
		DBCursor<ASRAnnotation> tvc = mongoStorer.getDBCollection(ASRAnnotation.class).find(textQ, projection).sort(sorting);
		log.debug(String.format("Created cursor with %s results for '%s' in %s ms. ", tvc.count(), textQuery, (System.currentTimeMillis() - start)));
		return cleanASRAnnotations(mongoStorer.toScoredSet(tvc, defaultMax, "Found via text search", "score").asList());
	}

	@Override
	public List<ASRAnnotation> findASRAnnotationsForTVProg(String tvProgUri) {
		if (tvProgUri == null) return ImmutableList.of();
		if (typeResolver.isTVProgram(tvProgUri)) {
			DBCursor<ASRAnnotation> cursor = mongoStorer.getDBCollection(ASRAnnotation.class).find().in("inSegment.partOf._id", ImmutableList.of(tvProgUri));
			return cleanASRAnnotations(cursor.toArray(defaultMax));
		} else {
			log.warn(String.format("Cannot find ASRAnnotations for uri %s of type %s", tvProgUri, typeResolver.resolveType(tvProgUri)));
			return ImmutableList.of();
		}
	}

	@Override
	public List<OCRAnnotation> findOCRAnnotationsFor(TVProgramBean mediaResource) {
		if (mediaResource == null) return ImmutableList.of();
		final String tvProgUri = mediaResource.getUrl();
		if (tvProgUri == null) {
			log.warn("Cannot retrieve OCR annotations for tvProgramBean with null uri " + mediaResource);
			return ImmutableList.of();
		}
		if (typeResolver.isTVProgram(tvProgUri)) {
			DBCursor<OCRAnnotation> cursor = mongoStorer.getDBCollection(OCRAnnotation.class).find().in("inSegment.partOf._id", ImmutableList.of(tvProgUri));
			return cleanOCRAnnotations(cursor.toArray(defaultMax));
		} else {
			log.warn(String.format("Cannot find OCRAnnotations for uri %s of type %s", tvProgUri, typeResolver.resolveType(tvProgUri)));
			return ImmutableList.of();
		}
	}

	@Override
	public Optional<OCRAnnotation> findOCRAnnotation(String ocrAnnotUri) {
		if (ocrAnnotUri == null) {
			log.warn("Cannot retrieve OCR annotations for null uri ");
			return Optional.absent();
		}
		if (typeResolver.isOCRAnnotation(ocrAnnotUri)) {
			DBCursor<OCRAnnotation> cursor = mongoStorer.getDBCollection(OCRAnnotation.class).find().in("_id", ImmutableList.of(ocrAnnotUri));
			List<OCRAnnotation> list = cleanOCRAnnotations(cursor.toArray(defaultMax));
			if (!list.isEmpty()) {
				return Optional.fromNullable(list.get(0));
			} else return Optional.absent();
		} else {
			log.warn(String.format("Cannot find OCRAnnotations for uri %s of type %s", ocrAnnotUri, typeResolver.resolveType(ocrAnnotUri)));
			return Optional.absent();
		}
	}
	
	@Override
	public List<OCRAnnotation> findAllOCRAnnotations(int limit) {
		DBCursor<OCRAnnotation> cursor = mongoStorer.getDBCollection(OCRAnnotation.class).find();
		log.debug(String.format("Found %s OCRAnnotations", cursor.count()));
		if (cursor.count() > limit) 
			return cleanOCRAnnotations(cursor.skip(cursor.count() - limit).toArray(limit));
		return cleanOCRAnnotations(cursor.toArray(limit));
	}

	@Override
	public List<OCRAnnotation> findOCRAnnotationsByText(String textQuery) {
		DBObject textQ = new BasicDBObject(
			    "$text", new BasicDBObject("$search", textQuery)
				);
		DBObject projection = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				);
		DBObject sorting = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				); 
		long start = System.currentTimeMillis();
		DBCursor<OCRAnnotation> tvc = mongoStorer.getDBCollection(OCRAnnotation.class).find(textQ, projection).sort(sorting);
		log.debug(String.format("Created cursor with %s results for '%s' in %s ms. ", tvc.count(), textQuery, (System.currentTimeMillis() - start)));
		return cleanOCRAnnotations(mongoStorer.toScoredSet(tvc, defaultMax, "Found via text search", "score").asList());
	}

	@Override
	public List<SubtitleSegment> findSubtitleSegmentsForTVProg(String tvProgUri) {
		if (tvProgUri == null) return ImmutableList.of();
		if (typeResolver.isTVProgram(tvProgUri)) {
			DBCursor<SubtitleSegment> cursor = mongoStorer.getDBCollection(SubtitleSegment.class).find().in("partOf.partOf._id", ImmutableList.of(tvProgUri));
			return cleanSubTitleSegments(cursor.toArray(defaultMax));
		} else {
			log.warn(String.format("Cannot find subtitle segments for uri %s of type %s", tvProgUri, typeResolver.resolveType(tvProgUri)));
			return ImmutableList.of();
		}
	}


	@Override
	public List<SubtitleSegment> findSubtitleSegmentsByText(String textQuery) {
		DBObject textQ = new BasicDBObject(
			    "$text", new BasicDBObject("$search", textQuery)
				);
		DBObject projection = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				);
		DBObject sorting = new BasicDBObject(
				"score", new BasicDBObject("$meta", "textScore")
				); 
		long start = System.currentTimeMillis();
		DBCursor<SubtitleSegment> tvc = mongoStorer.getDBCollection(SubtitleSegment.class).find(textQ, projection).sort(sorting);
		log.debug(String.format("Created cursor with %s results for '%s' in %s ms. ", tvc.count(), textQuery, (System.currentTimeMillis() - start)));
		return cleanSubTitleSegments(mongoStorer.toScoredSet(tvc, defaultMax, "Found via text search", "score").asList());
	}

	@Override
	public List<SubtitleSegment> findAllSubtitleSegments(int limit) {
		DBCursor<SubtitleSegment> cursor = mongoStorer.getDBCollection(SubtitleSegment.class).find();
		log.debug(String.format("Found %s SubtitleSegs", cursor.count()));
		if (cursor.count() > limit) 
			return cleanSubTitleSegments(cursor.skip(cursor.count() - limit).toArray(limit));
		return cleanSubTitleSegments(cursor.toArray(limit));
	}

	@Override
	public List<SubtitleSegment> findAllSubtitleSegmentsByDate(long dateFrom,
			long dateTo, int limit) {
		String timeStampBeanPath = "partOf.startTime.timestamp";
		Date to = new Date(dateTo);
		Date from = new Date(dateFrom);
		Query q = DBQuery.lessThan(timeStampBeanPath, to).greaterThanEquals(timeStampBeanPath, from);
		DBCursor<SubtitleSegment> cursor = mongoStorer.getDBCollection(SubtitleSegment.class).find(q);
		log.debug(String.format("Found %s SubtitleSegs between %s and %s", cursor.count(), from, to));
		return cleanSubTitleSegments(cursor.toArray(limit));
	}

	public <T extends XLiMeResource> Optional<T> findResource(
			Class<T> resourceClass, String resourceUrl) {
		return mongoStorer.findResource(resourceClass, resourceUrl);
	}
	
}
