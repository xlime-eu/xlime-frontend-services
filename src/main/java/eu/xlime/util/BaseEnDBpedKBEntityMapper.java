package eu.xlime.util;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import eu.xlime.bean.StatMetrics;

/**
 * Provides a base implementation for a {@link KBEntityMapper} that uses English DBpedia as its 
 * canonical namespace. All other uris are mapped to English DBpedia uris, when possible.
 * 
 * @author rdenaux
 *
 */
public abstract class BaseEnDBpedKBEntityMapper implements KBEntityMapper {

	private static final Logger log = LoggerFactory.getLogger(BaseEnDBpedKBEntityMapper.class);

	private static class MappingStats {
		public long canonicalisations;
		public long timeMs;
		public long unknownKBEnts;
		public long mainDBpediaEnts;
		public long encodedMainDBpediaEnts;
		public long fromLangDBpEnts;
		public long convertedFromLangDBpEnts;
		public long failedFromLangDBpEnts;
		public long fromLangDBpEntsTimeMs;
		public long fromWikiEnts;
		public long convertedFromWikiEnts;
		public long failedFromWikiEnts;
		public long fromWikiEntsTimeMs;
		
		public String toString() {
			return String.format("Canonicalisations=%s, timeMs=%s, unknownEnts=%s, mainDBpediaEnts=%s, encodedMainDBpediaEnts=%s, fromLangDBpedia=[total=%s, converted=%s, failed=%s, time=%sms], fromWiki=[total=%s, converted=%s, failed=%s, time=%sms]",
					canonicalisations, timeMs, unknownKBEnts, mainDBpediaEnts, encodedMainDBpediaEnts, fromLangDBpEnts, convertedFromLangDBpEnts, failedFromLangDBpEnts, fromLangDBpEntsTimeMs, fromWikiEnts, convertedFromWikiEnts, failedFromWikiEnts, fromWikiEntsTimeMs);
		}
		public Map<String, Long> toCounterMap() {
			Map<String, Long> result = new HashMap<String, Long>();
			result.put("canonicalisations", canonicalisations);
			result.put("timeMs", timeMs);
			result.put("unknownKBEnts", unknownKBEnts);
			result.put("mainDBpediaEnts", mainDBpediaEnts);
			result.put("encodedMainDBpediaEnts", encodedMainDBpediaEnts);
			result.put("fromLangDBpEnts", fromLangDBpEnts);
			result.put("convertedFromLangDBpEnts", convertedFromLangDBpEnts);
			result.put("failedFromLangDBpEnts", failedFromLangDBpEnts);
			result.put("fromLangDBpEntsTimeMs", fromLangDBpEntsTimeMs);
			result.put("fromWikiEnts", fromWikiEnts);
			result.put("convertedFromWikiEnts", convertedFromWikiEnts);
			result.put("failedFromWikiEnts", failedFromWikiEnts);
			result.put("fromWikiEntsTimeMs", fromWikiEntsTimeMs);
				
			return ImmutableMap.copyOf(result);
		}
	}
	
	private static MappingStats stats = new MappingStats();
	
	private static final Date statsStartDate = new Date();

	private static Set<String> langWhitelist = ImmutableSet.of("en", "de", "es", "fr", "zh", "it");

	/**
	 * Returns a {@link StatMetrics} instance for the mapping tasks performed by this instances of 
	 * this base class.
	 * 
	 * @return
	 */
	public final static StatMetrics getStatMetrics() {
		StatMetrics result = new StatMetrics();
		result.setMeterId(BaseEnDBpedKBEntityMapper.class.getSimpleName());
		result.setMeterStartDate(statsStartDate);
		result.addCounters(stats.toCounterMap());
		return result;
	}
	
	/**
	 * Returns the <code>owl:sameAs</code> closure for a given entityUri.
	 *  
	 * @param entUri
	 * @return the <code>owl:sameAs</code> closure for a given entityUri, <b>including entUri itself</b>
	 * @throws ExecutionException
	 */
	abstract protected Set<String> getDBpediaSameAsSet(final String entUri) throws ExecutionException;
	
	/**
	 * Returns the <code>canonical</code> entity uri for a given set
	 * @param set
	 * @param langDepUrl
	 * @return
	 */
	protected final Optional<String> findMainDBpedia(Set<?> set, String langDepUrl) {
		Set<String> mainDBpedia = new HashSet<>();
		for (Object url: set) {
			if (url instanceof String) {
				String surl = (String)url;
				KBEntityUri kburi = KBEntityUri.of(surl); 
				if (kburi.isMainDBpediaEntity()) mainDBpedia.add(kburi.asIri());
			}
		}
		if (mainDBpedia.isEmpty()) {
			log.debug("Found no main dbpedia entity in " + set);
			return Optional.absent();
		} else if (mainDBpedia.size() > 1) {
			log.debug("Found multiple main dbpedia entities " + mainDBpedia);
			//TODO: find a way to select the best option (e.g. Lünen, L%C3#BCnen,_Germany, Lünen,_Germany, Lunen) 
		}
		return Optional.of(mainDBpedia.iterator().next());
	}
	
	/* (non-Javadoc)
	 * @see eu.xlime.util.KBEntityMapper#toCanonicalEntityUrl(java.lang.String)
	 */
	@Override
	public final Optional<String> toCanonicalEntityUrl(String entUrl) {
		stats.canonicalisations++;
		summariseStats();
		final long canonStart = System.currentTimeMillis();
		final KBEntityUri eUri = new KBEntityUri(entUrl);
		try {
			if (eUri.isMainDBpediaEntity()) {
				log.trace("Entity " + entUrl + " is already canonical");
				if (eUri.isEncoded()) {
					stats.encodedMainDBpediaEnts++;
				}
				stats.mainDBpediaEnts++;
				return Optional.of(eUri.asIri());
			}
			if (eUri.isLangDependentDBpediaEntity()) {
				log.trace("Entity " + entUrl + " is from language-dependent dbpedia");
				stats.fromLangDBpEnts++;
				return toMainDBpedia(entUrl);
			}
			if (eUri.isWikiEnt()) {
				log.trace("Entity " + entUrl + " is from wikipedia");
				stats.fromWikiEnts++;
				final long start = System.currentTimeMillis();
				Optional<String> result = toMainDBpedia(eUri.rewriteWikiToDBpedia(langWhitelist));
				if (result.isPresent()) stats.convertedFromLangDBpEnts++;
				else stats.failedFromLangDBpEnts++;
				stats.fromWikiEntsTimeMs += (System.currentTimeMillis() - start);
				return result;
			}
			log.warn("Entity is from unknown KB " + entUrl);
			stats.unknownKBEnts++;
			return Optional.absent();
		} finally {
			stats.timeMs += (System.currentTimeMillis() - canonStart);
		}
	}

	/* (non-Javadoc)
	 * @see eu.xlime.util.KBEntityMapper#expandSameAs(java.lang.String)
	 */
	@Override
	public final Set<String> expandSameAs(String entUrl) {
		KBEntityUri eUri = new KBEntityUri(entUrl);
		if (eUri.isMainDBpediaEntity()) {
			log.trace("Entity " + entUrl + " is already canonical");
			return expandSameAsSetFromDBpedia(entUrl);
		}
		if (eUri.isLangDependentDBpediaEntity()) {
			log.trace("Entity " + entUrl + " is from language-dependent dbpedia");			
			return expandSameAsSetFromDBpedia(entUrl);
		}
		if (eUri.isWikiEnt()){
			log.trace("Entity " + entUrl + " is from wikipedia");
			return expandSameAsSetFromDBpedia(eUri.rewriteWikiToDBpedia(langWhitelist));
		}
		log.trace("Entity is from unknown KB " + entUrl);
		return ImmutableSet.of(entUrl);
	}

	
	private Set<String> expandSameAsSetFromDBpedia(
			Optional<String> langDBpediaRes) {
		if (langDBpediaRes.isPresent()) return expandSameAsSetFromDBpedia(langDBpediaRes.get());
		else return ImmutableSet.of();
	}

	private Set<String> expandSameAsSetFromDBpedia(String entUrl) {
		Set<String> dbpediaResSet = ImmutableSet.of(entUrl);
		try {
			dbpediaResSet = getDBpediaSameAsSet(entUrl);
		} catch (ExecutionException e) {
			log.warn("Error expanding sameAs set for " + entUrl, e);
		}
		Set<String> wikiEnts = new HashSet<String>();
		for (String dbpres: dbpediaResSet) {
			KBEntityUri eUri = new KBEntityUri(dbpres);
			if (eUri.isLangDependentDBpediaEntity()) {
				Optional<String> optWikiUrl = eUri.rewriteDBpediaToWiki();
				if (optWikiUrl.isPresent())
					wikiEnts.add(optWikiUrl.get());
			}
		}
		return ImmutableSet.<String>builder().addAll(dbpediaResSet).addAll(wikiEnts).build();
	}
	
	private Optional<String> toMainDBpedia(Optional<String> langDBpediaRes) {
		if (langDBpediaRes.isPresent()) return toMainDBpedia(langDBpediaRes.get());
		else return langDBpediaRes;
	}

	private Optional<String> toMainDBpedia(final String langDBpediaRes) {
		KBEntityUri kbUri = KBEntityUri.of(langDBpediaRes); 
		if (kbUri.isMainDBpediaEntity()) return Optional.of(kbUri.asIri());
				
		long start = System.currentTimeMillis();
		Optional<String> result = Optional.absent();
		try {
			log.trace("Retrieving main DBpedia resource for " + langDBpediaRes);
			Set<String> sameAsSet = getDBpediaSameAsSet(langDBpediaRes);
			result = findMainDBpedia(sameAsSet, langDBpediaRes);
			log.trace("Retrieved main DBpedia resource " + result);
		} catch (ExecutionException e) {
			log.warn("Error loading sameAs values for " + langDBpediaRes, e);
		}
		if (result.isPresent()) stats.convertedFromLangDBpEnts++;
		else stats.failedFromLangDBpEnts++;
		stats.fromLangDBpEntsTimeMs += (System.currentTimeMillis() - start);
		return result;
	}
		
	private void summariseStats() {
		if (stats.canonicalisations % 10000 == 0) {
			log.info(stats.toString());
		}
	}
		
}
