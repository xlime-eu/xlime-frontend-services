package eu.xlime.dao.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import eu.xlime.dao.UIEntityDao;
import eu.xlime.summa.bean.UIEntity;

public abstract class AbstractUIEntityDao implements UIEntityDao {

	
	@Override
	public final Optional<UIEntity> retrieveFromUri(String entUri) {
		return retrieveFromUri(entUri, Optional.of(getDefaultLocale()));
	}

	@Override
	public final List<UIEntity> retrieveFromUris(List<String> uris) {
		return retrieveFromUris(uris, Optional.of(getDefaultLocale()));
	}

	/**
	 * Returns the default {@link Locale} to use when retrieving {@link UIEntity}s.
	 * @return
	 */
	protected abstract Locale getDefaultLocale();

	/**
	 * <i>Cleans</i> an input entity and returns an entity which is better suitable to 
	 * be used in the front-end. For example, some types may be removed, or the list of 
	 * types may be sorted to put more common types at the beginning of the list.
	 *   
	 * @param entity
	 * @return
	 */
	protected UIEntity cleanEntity(UIEntity entity) {
		Set<String> cleanTypes = new HashSet<>(entity.getTypes());
		for (String type: entity.getTypes()) {
			if (type.startsWith("http://dbpedia.org/class/yago/")) 
				cleanTypes.remove(type);
		}
		entity.setTypes(ImmutableList.copyOf(cleanTypes));
		return entity;
	}
	
	protected List<UIEntity> cleanEntities(List<UIEntity> ents) {
		List<UIEntity> result = new ArrayList<>();
		for (UIEntity toClean: ents) {
			result.add(cleanEntity(toClean));
		}
		return ImmutableList.copyOf(result);
	}
	
}
