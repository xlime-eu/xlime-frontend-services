package eu.xlime.dao.annotation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

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
import eu.xlime.dao.UIEntityDao;
import eu.xlime.dao.entity.MongoUIEntityDao;
import eu.xlime.dao.mediaitem.MongoMediaItemDao;
import eu.xlime.mongo.DBCollectionProvider;
import eu.xlime.summa.bean.UIEntity;
import eu.xlime.util.ListUtil;
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
	private final MongoUIEntityDao uiEntDao;
	private final DBCollectionProvider collectionProvider;
	private static final ResourceTypeResolver typeResolver = new ResourceTypeResolver();
	private static final ScoreFactory scoref = ScoreFactory.instance;

	private final int defaultMax = 30;
	
	public MongoMediaItemAnnotationDao(Properties props) {
		collectionProvider = new DBCollectionProvider(props);
		mongoStorer = new MongoXLiMeResourceStorer(collectionProvider);
		miDao = new MongoMediaItemDao(props);
		uiEntDao = new MongoUIEntityDao(props);
	}
	
	@Override
	public List<XLiMeResource> findRecentAnnotations(int minutes, int limit) {
		final boolean ascending = true;
		int partialLimit = (limit / 3);
		if (partialLimit == 0) partialLimit = limit;
		List<EntityAnnotation> entAnns = mongoStorer.getSortedByDate(EntityAnnotation.class, !ascending, partialLimit);
		List<SubtitleSegment> subtitles = mongoStorer.getSortedByDate(SubtitleSegment.class, !ascending, partialLimit);
		List<ASRAnnotation> asrAnns = mongoStorer.getSortedByDate(ASRAnnotation.class, !ascending, partialLimit);		
		List<OCRAnnotation> ocrAnns = mongoStorer.getSortedByDate(OCRAnnotation.class, !ascending, partialLimit);
		List<XLiMeResource> result = new ListUtil().weave(entAnns, subtitles, asrAnns, ocrAnns);
		if (result.size() > limit) {
			return ImmutableList.copyOf(uiClean(result.subList(0, limit)));
		} else return ImmutableList.copyOf(uiClean(result));
	}


	private List<XLiMeResource> uiClean(List<XLiMeResource> list) {
		List<XLiMeResource> result = new ArrayList<>();
		for (XLiMeResource res: list) {
			result.add(uiCleanResource(res));
		}
		return result;
	}

	@Override
	protected Optional<MediaItemDao> getMediaItemDao() {
		return Optional.<MediaItemDao>of(miDao);
	}
	
	@Override
	protected Optional<UIEntityDao> getUIEntDao() {
		return Optional.<UIEntityDao>of(uiEntDao);
	}

	public List<EntityAnnotation> findEntityAnnotations(int limit) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find();
		log.debug(String.format("Found random %s EntAnns", cursor.count()));
		return cursor.toArray(limit);
	}
	
	@Override
	public List<EntityAnnotation> findMicroPostEntityAnnotations(
			String microPostUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(microPostUrl));
		log.debug(String.format("Found %s EntAnns for micropost %s", cursor.count(), microPostUrl));
		return cursor.toArray(defaultMax);
	}

	@Override
	public List<EntityAnnotation> findNewsArticleEntityAnnotations(
			String newsArticleUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(newsArticleUrl));
		log.debug(String.format("Found %s EntAnns for news %s", cursor.count(), newsArticleUrl));
		return cursor.toArray(defaultMax);
	}

	@Override
	public List<EntityAnnotation> findSubtitleTrackEntityAnnotations(
			String subtitleTrackUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(subtitleTrackUrl));
		log.debug(String.format("Found %s EntAnns for subTrack %s", cursor.count(), subtitleTrackUrl));
		return cursor.toArray(defaultMax);
	}
	
	@Override
	public List<EntityAnnotation> findTVSubtitleEntityAnnotations(
			String tvProgUrl) {
		List<EntityAnnotation> ents = new ArrayList<EntityAnnotation>();
		for (SubtitleSegment sseg: findSubtitleSegmentsForTVProg(tvProgUrl)) {
			ents.addAll(findSubtitleEntityAnnotations(sseg.getUrl()));
		}
		return ents;
	}

	@Override
	public List<EntityAnnotation> findSubtitleEntityAnnotations(String subUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(subUrl));
		log.debug(String.format("Foudn %s EntAnns for sub %s", cursor.count(), subUrl));
		return cursor.toArray(defaultMax);
	}

	@Override
	public List<EntityAnnotation> findAudioTrackEntityAnnotations(
			String audioTrackUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(audioTrackUrl));
		log.debug(String.format("Found %s EntAnns for audio %s", cursor.count(), audioTrackUrl));
		return cursor.toArray(defaultMax);
	}

	@Override
	public List<EntityAnnotation> findTVASREntityAnnotations(String tvProgUrl) {
		List<EntityAnnotation> ents = new ArrayList<EntityAnnotation>();
		List<ASRAnnotation> asrAnns = findASRAnnotationsForTVProg(tvProgUrl);
		for (ASRAnnotation asrAnn: asrAnns.subList(0, Math.min(5, asrAnns.size()))) {
			ents.addAll(findASREntityAnnotations(asrAnn.getUrl()));
		}
		return ents;
	}

	@Override
	public List<EntityAnnotation> findASREntityAnnotations(String asrUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(asrUrl));
		log.debug(String.format("Found %s EntAnns for asr %s", cursor.count(), asrUrl));
		return cursor.toArray(defaultMax);
	}

	@Override
	public List<EntityAnnotation> findVideoTrackEntityAnnotations(
			String videoTrackUrl) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("resourceUrl", ImmutableList.of(videoTrackUrl));
		log.debug(String.format("Found %s EntAnns for vidTrack %s", cursor.count(), videoTrackUrl));
		return cursor.toArray(defaultMax);
	}

	@Override
	public List<EntityAnnotation> findEntityAnnotationsFor(UIEntity kbEntity) {
		DBCursor<EntityAnnotation> cursor = mongoStorer.getDBCollection(EntityAnnotation.class).find().in("entity._id", ImmutableList.of(kbEntity.getUrl()));
		log.debug(String.format("Found %s EntAnns for KBEnt %s", cursor.count(), kbEntity));
		return cursor.toArray(defaultMax);
	}

	@Override
	public ScoredSet<String> findMediaItemUrlsByKBEntity(String entityUrl) {
		DBCursor<EntityAnnotation> socCursor = mongoStorer.getDBCollection(EntityAnnotation.class).find()
				.in("entity._id", ImmutableList.of(entityUrl))
				.regex("resourceUrl", Pattern.compile("^http...vico"))
				.sort(DBSort.desc("confidence"));
		DBCursor<EntityAnnotation> newsCursor = mongoStorer.getDBCollection(EntityAnnotation.class).find()
				.in("entity._id", ImmutableList.of(entityUrl))
				.regex("resourceUrl", Pattern.compile("^http...ijs"))
				.sort(DBSort.desc("confidence"));
		DBCursor<EntityAnnotation> tvCursor = mongoStorer.getDBCollection(EntityAnnotation.class).find()
				.in("entity._id", ImmutableList.of(entityUrl))
				.regex("resourceUrl", Pattern.compile("^http...zattoo"))
				//.sort(DBSort.desc("confidence"))// for some reason, this takes an order of magnitude longer than the others when sorting??
				;
		log.debug(String.format("Found %s micropost, %s news and %s tv EntAnns for entity %s", 
				socCursor.count(), newsCursor.count(), tvCursor.count(), entityUrl));
		return ScoredSetImpl.<String>builder()
			.addAll(toMediaItemUrlScoredSet(tvCursor, 5))
			.addAll(toMediaItemUrlScoredSet(socCursor, 5))
			.addAll(toMediaItemUrlScoredSet(newsCursor, 5))
			.build();
	}
	
	@Override
	public ScoredSet<ASRAnnotation> findASRAnnotationsForKBEntity(
			String entityUrl) {
		//EntityAnnotation.resourceUrl only refer to the tv program track (audio indicates some ASRAnnotation was entity linked, but we don't know which one...
		return ScoredSetImpl.empty();
	}

	@Override
	public ScoredSet<OCRAnnotation> findOCRAnnotationForKBEntity(
			String entityUrl) {
		//EntityAnnotation.resourceUrl only refer to the tv program track (video indicates some OCRAnnotation was entity linked, but we don't know which one...
		return ScoredSetImpl.empty();
	}

	@Override
	public ScoredSet<SubtitleSegment> findSubtitleSegmentsForKBEntity(
			String entityUrl) {
		//EntityAnnotation.resourceUrl only refer to the tv program track (subtitle means some SubtitleSegment was entity linked, but we don't know which one...
		return ScoredSetImpl.empty();
	}

	private ScoredSet<String> toMediaAnnotUrlScoredSet(DBCursor<EntityAnnotation> cursor) {
		ScoredSet.Builder<String> builder = ScoredSetImpl.builder();
		for (EntityAnnotation ea: cursor.toArray(defaultMax)) {
			Optional<String> miUrl = mapAnnotatedResourceUrlToMediaAnnotUrl(ea.getResourceUrl());
			if (miUrl.isPresent()) {
				builder.add(miUrl.get(), toScore(ea));
			}
		}
		return builder.build();
	}

	private ScoredSet<String> toMediaItemUrlScoredSet(
			DBCursor<EntityAnnotation> cursor, int limit) {
		if (limit < 1) return ScoredSetImpl.empty();
		final long start = System.currentTimeMillis();
		ScoredSet.Builder<String> builder = ScoredSetImpl.builder();
		List<EntityAnnotation> eas = cursor.limit(limit).toArray();
		log.debug("Retrieved EntAnns from cursor");
		for (EntityAnnotation ea: eas) {
			Optional<String> miUrl = mapAnnotatedResourceUrlToMediaItemUrl(ea.getResourceUrl());
			if (miUrl.isPresent()) {
				builder.add(miUrl.get(), toScore(ea));
			}
		}
		log.debug("Mapped EntAnns to mediaItemUrls");
		final ScoredSet<String> result = builder.build();
		final long ms = System.currentTimeMillis() - start;
		log.debug(String.format("Converted entAnn cursor to %s scoredUrls in %s ms", result.size(), ms));
		return result;
	}
	
	private ScoredSet<String> toMediaItemUrlScoredSet(
			DBCursor<EntityAnnotation> cursor) {
		return toMediaItemUrlScoredSet(cursor, defaultMax);
	}

	protected final Optional<String> mapAnnotatedResourceUrlToMediaItemUrl(
			String resourceUrl) {
		if (typeResolver.isMediaItem(resourceUrl)) return Optional.of(resourceUrl);
		try {
			if (typeResolver.isSubtitleTrack(resourceUrl)) {
				return typeResolver.subtitleTrackUrlAsTVProgUrl(resourceUrl);
			} else if (typeResolver.isAudioTrack(resourceUrl)) {
				return typeResolver.audioTrackUrlAsTVProgUrl(resourceUrl);
			} else if (typeResolver.isVideoTrack(resourceUrl)) {
				return typeResolver.videoTrackUrlAsTVProgUrl(resourceUrl);
			} else if (typeResolver.isASRAnnotation(resourceUrl)) {
				return typeResolver.asrAnnUrlAsTVProgUrl(resourceUrl);
			}
		} catch (Exception e) {
			log.error(String.format("Error mapping annotated resource %s to a mediaItem url", resourceUrl));
		}
		return Optional.absent();
	}
	
	protected final Optional<String> mapAnnotatedResourceUrlToMediaAnnotUrl(String entityAnnResourceUrl) {
		if (typeResolver.isSubtitleSegmentUri(entityAnnResourceUrl) ||
				typeResolver.isASRAnnotation(entityAnnResourceUrl) ||
				typeResolver.isOCRAnnotation(entityAnnResourceUrl)) {
			return Optional.of(entityAnnResourceUrl);
		} 
		return Optional.absent();
	}

	private Score toScore(EntityAnnotation ea) {
		return scoref.newScore(ea.getConfidence(), "Entity annotation on " + ea.getResourceUrl());
	}

	@Override
	public Optional<ASRAnnotation> findASRAnnotation(String asrAnnotUri) {
		if (asrAnnotUri == null) {
			log.warn("Cannot retrieve ASR annotation for null uri ");
			return Optional.absent();
		}
		if (typeResolver.isASRAnnotation(asrAnnotUri)) {
			DBCursor<ASRAnnotation> cursor = mongoStorer.getDBCollection(ASRAnnotation.class).find().in("_id", ImmutableList.of(asrAnnotUri));
			List<ASRAnnotation> list = cleanASRAnnotations(cursor.toArray(defaultMax));
			if (!list.isEmpty()) {
				return Optional.fromNullable(list.get(0));
			} else return Optional.absent();
		} else {
			log.warn(String.format("Cannot find SubtitleSegments for uri %s of type %s", asrAnnotUri, typeResolver.resolveType(asrAnnotUri)));
			return Optional.absent();
		}
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
	public Optional<SubtitleSegment> findSubtitleSegment(
			String subtitleSegmentUri) {
		if (subtitleSegmentUri == null) {
			log.warn("Cannot retrieve SubtitleSegment for null uri ");
			return Optional.absent();
		}
		if (typeResolver.isSubtitleSegmentUri(subtitleSegmentUri)) {
			DBCursor<SubtitleSegment> cursor = mongoStorer.getDBCollection(SubtitleSegment.class).find().in("_id", ImmutableList.of(subtitleSegmentUri));
			List<SubtitleSegment> list = cleanSubTitleSegments(cursor.toArray(defaultMax));
			if (!list.isEmpty()) {
				return Optional.fromNullable(list.get(0));
			} else return Optional.absent();
		} else {
			log.warn(String.format("Cannot find SubtitleSegments for uri %s of type %s", subtitleSegmentUri, typeResolver.resolveType(subtitleSegmentUri)));
			return Optional.absent();
		}
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
