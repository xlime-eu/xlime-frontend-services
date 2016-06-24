package eu.xlime.dao.entity;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.mongojack.DBCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import eu.xlime.bean.UrlLabel;
import eu.xlime.dao.MongoXLiMeResourceStorer;
import eu.xlime.dao.annotation.MongoMediaItemAnnotationDao;
import eu.xlime.mongo.DBCollectionProvider;
import eu.xlime.search.AutocompleteBean;
import eu.xlime.search.AutocompleteClient;
import eu.xlime.summa.bean.UIEntity;

public class MongoUIEntityDao extends AbstractUIEntityDao {

	private static final Logger log = LoggerFactory.getLogger(MongoUIEntityDao.class);
	
	private final MongoXLiMeResourceStorer mongoStorer;
	private final DBCollectionProvider collectionProvider;

	private int defaultLimit = 30;
	public MongoUIEntityDao(Properties props) {
		collectionProvider = new DBCollectionProvider(props);
		mongoStorer = new MongoXLiMeResourceStorer(collectionProvider);
	}
	
	@Override
	public Optional<UIEntity> retrieveFromUri(String entUri, Optional<Locale> locale) {
		DBCursor<UIEntity> cursor = mongoStorer.getDBCollection(UIEntity.class, locale).find().in("_id", ImmutableList.of(entUri));
		log.debug(String.format("Found %s EntAnns", cursor.count()));
		return cursor.hasNext() ? Optional.of(cursor.next()) : Optional.<UIEntity>absent();
	}

	@Override
	public List<UIEntity> retrieveFromUris(List<String> uris,
			Optional<Locale> locale) {
		DBCursor<UIEntity> cursor = mongoStorer.getDBCollection(UIEntity.class, locale).find().in("_id", uris);
		log.debug(String.format("Found %s EntAnns", cursor.count()));
		return cursor.toArray(defaultLimit);
	}

	@Override
	public List<UrlLabel> autoCompleteEntities(String text) {
		//TODO: use mongoDB to implement autocomplete? see e.g. http://stackoverflow.com/questions/8223841/implement-autocomplete-on-mongodb#8228510
		AutocompleteClient client = new AutocompleteClient();
		Optional<AutocompleteBean> autocomplete = client.retrieveAutocomplete(text);
		if (!autocomplete.isPresent()) return ImmutableList.of();
		List<UrlLabel> entities = autocomplete.get().getEntities();
		Ordering<UrlLabel> byPresence = Ordering.natural().reverse().onResultOf(new Function<UrlLabel, Integer>() {

			@Override
			public Integer apply(UrlLabel input) {
				return mongoStorer.getDBCollection(UIEntity.class).find().in("_id", ImmutableList.of(input.getUrl())).count();
			}
			
		});
		long start = System.currentTimeMillis();
		List<UrlLabel> sorted = byPresence.sortedCopy(entities);
		log.debug(String.format("Sorted %s autocompletions in %s ms", entities.size(), (System.currentTimeMillis() - start)));
		return sorted;
	}

	@Override
	protected Locale getDefaultLocale() {
		return Locale.UK;
	}

}
