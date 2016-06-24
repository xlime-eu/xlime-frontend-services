package eu.xlime.summa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.google.common.base.Optional;

import eu.xlime.bean.UrlLabel;
import eu.xlime.dao.entity.UIEntityDaoImpl;
import eu.xlime.summa.bean.EntitySummary;
import eu.xlime.summa.bean.SummaryStatement;
import eu.xlime.summa.bean.UIEntity;

/**
 * Converts a RDF {@link Model} following the Summa RDF model into an {@link EntitySummary}.
 * 
 * @author RDENAUX
 *
 */
public class EntitySummaryFromRDFModelFactory {

	private static final String SUMMARY = "http://purl.org/voc/summa/Summary";	
	private static final String ENTITY = "http://purl.org/voc/summa/entity";
	private static final String TOP_K = "http://purl.org/voc/summa/topK";
	private static final String STATEMENT = "http://purl.org/voc/summa/statement";
	
	private static final String HAS_RANK = "http://purl.org/voc/vrank#hasRank";
	private static final String RANK_VALUE = "http://purl.org/voc/vrank#rankValue";
	
	private final Model rdfModel;
	private final ValueFactory valFact;
	
	
	public EntitySummaryFromRDFModelFactory(Model rdfModel) {
		super();
		this.rdfModel = rdfModel;
		valFact = rdfModel.getValueFactory();
	}


	public Optional<EntitySummary> extractFromModel() {
		Resource summaRes = extractSummaResource(rdfModel, valFact);
		
		EntitySummary result = new EntitySummary();
		UIEntity entity = extractSummarisedEntity(summaRes); 
		result.setEntity(entity);
		result.setStatements(extractStatements(summaRes, entity.getUrl()));
		result.setTopK(extractTopK(summaRes));
		return Optional.of(result);
	}
	
	private int extractTopK(Resource summaRes) {
		return rdfModel.filter(summaRes, valFact.createURI(TOP_K), null).objectLiteral().intValue();
	}


	private List<SummaryStatement> extractStatements(Resource summaRes, String summaResEntUrl) {
		Set<Value> statementVals = rdfModel.filter(summaRes, valFact.createURI(STATEMENT), null).objects();
		List<SummaryStatement> result = new ArrayList<>();
		for (Value statVal: statementVals) {
			result.add(asSummaryStatement((Resource)statVal, summaResEntUrl));
		}
		return result;
	}

	private SummaryStatement asSummaryStatement(Resource statRes, String entUrl) {
		Resource subject = rdfModel.filter(statRes, RDF.SUBJECT, null).objectResource();
		URI pred = rdfModel.filter(statRes, RDF.PREDICATE, null).objectURI();
		Value obj = rdfModel.filter(statRes, RDF.OBJECT, null).objectValue();
		
		Value rankStatement = rdfModel.filter(statRes, valFact.createURI(HAS_RANK), null).objectValue();
		double rankValue = rdfModel.filter((Resource) rankStatement, valFact.createURI(RANK_VALUE), null).objectLiteral().floatValue();
		
		SummaryStatement result = new SummaryStatement();
		UrlLabel relation = asUrlLabel(pred);
		result.setRelation(relation);
		
		if (subject.stringValue().equals(entUrl)) {
			result.setRelated(asUrlLabel(obj));
		} else {
			result.setRelated(asUrlLabel(subject));
			relation.setLabel(relation.getLabel() + " of");
		}
		result.setRankValue(rankValue);
		return result;
	}


	private UrlLabel asUrlLabel(Value value) {
		if (value instanceof Resource) return asUrlLabel((Resource) value);
		if (value instanceof Literal) {
			Literal lit = (Literal)value;
			UrlLabel result = new UrlLabel();
			result.setLabel(lit.getLabel());
			return result;
		} 
		throw new RuntimeException("Cannot convert " + value + " into a UrlLabel");
	}


	private UIEntity extractSummarisedEntity(Resource summaRes) {
		Resource summaResEnt = rdfModel.filter(summaRes, valFact.createURI(ENTITY), null).objectResource();
		return asUIEntity(summaResEnt);
	}

	private UIEntity asUIEntity(Resource entResource) {
		UIEntity result = retrieveUIEntity(entResource.stringValue());
		result.setLabel(rdfModel.filter(entResource, RDFS.LABEL, null).objectString());
		return result;
	}

	private UIEntity retrieveUIEntity(String entUri) {
		return UIEntityDaoImpl.instance.retrieveFromUri(entUri).get();
	}


	private Resource extractSummaResource(Model model, ValueFactory valFact) {
		Set<Resource> summaRess = model.filter(null, RDF.TYPE, valFact.createURI(SUMMARY)).subjects();
		int foundSummaries = summaRess.size();
		if (foundSummaries != 1) throw new RuntimeException("Expecting only one summary, but found " + foundSummaries);
		Resource summaRes = summaRess.iterator().next();
		return summaRes;
	}

	private UrlLabel asUrlLabel(Resource entResource) {
		UrlLabel result = new UrlLabel();
		result.setUrl(entResource.stringValue());
		result.setLabel(rdfModel.filter(entResource, RDFS.LABEL, null).objectString());
		return result;
	}
	
}
