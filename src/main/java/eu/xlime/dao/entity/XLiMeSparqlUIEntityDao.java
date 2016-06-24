package eu.xlime.dao.entity;

import java.util.List;
import java.util.Locale;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import eu.xlime.bean.UrlLabel;
import eu.xlime.search.AutocompleteBean;
import eu.xlime.search.AutocompleteClient;
import eu.xlime.sparql.SparqlClient;
import eu.xlime.sparql.SparqlClientFactory;

public class XLiMeSparqlUIEntityDao extends BaseSparqlUIEntityDao {

	@Override
	protected SparqlClient getDBpediaSparqlClient() {
		return new SparqlClientFactory().getDBpediaSparqlClient(); 
//		return new SparqlClientFactory().getXliMeSparqlClient(); //relevant triples for entities already imported from DBpedia
	}

	@Override
	protected Locale getDefaultLocale() {
		return Locale.UK; //TODO: add config option to specify this...
	}

	@Override
	public List<UrlLabel> autoCompleteEntities(String text) {
		AutocompleteClient client = new AutocompleteClient();
		Optional<AutocompleteBean> autocomplete = client.retrieveAutocomplete(text);
		if (!autocomplete.isPresent()) return ImmutableList.of();
		List<UrlLabel> entities = autocomplete.get().getEntities();
		return entities;
	}
	
}
