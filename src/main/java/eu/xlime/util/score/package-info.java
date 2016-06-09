/**
 * Provides classes for manipulating and keeping track of scored objects. 
 * 
 * <h3>Motivation</h3>
 * 
 * The main use case is that you often want to keep track of one or more objects 
 * which have an associated score; typically to denote a confidence value about 
 * the veracity of the information represented by the object(s).
 * 
 * Another use-case is that besides the score for a given object, you also often 
 * would like to keep track of some justification structure for the score.
 * 
 * A naive approach for dealing with such cases is to use standard Java 
 * collections, for example a {@link java.util.Map} from the objects to some 
 * score value. The downside of this approach is that every time you manipulate 
 * the map, you have to figure out how to merge potentially existing score 
 * values. E.g. if an object already has a score value associated with it, do 
 * you override the previous score, or do you keep the best score, or the worst; 
 * and what do you do with the associated justifications? Since the Java collections 
 * API do not provide methods to deal with these situations, it is better to 
 * provide an alternative which simplifies manipulation of such collections.
 *  
 * Another minor disadvantage of java collections is that the collections are 
 * assumed to be <i>mutable</i> by default. In the case of scored collections of
 * objects this is important, since the scores may be normalized over the 
 * collection. Ideally, you want to avoid allowing clients to change the scored 
 * collections. 
 *
 * While more recent collection libraries such as Guava's collections solve some
 * of these issues, you'd still need to let the client to implement the merging 
 * and construction semantics for the collections.
 *
 * <h4>Motivation and use within KTagger</h4>
 * 
 * Within KTagger, the application of this package is fairly obvious, as we want
 * to keep track of annotations which may have different provenance, may use 
 * different evidence and thus may have a different confidence score. 
 * 
 * Between KTagger versions 1.0 and 1.5 we started using the kontology.fuzzylogic 
 * package which provides {@link com.isoco.kontology.fuzzylogic.sets.FuzzySet} 
 * and {@link com.isoco.kontology.fuzzylogic.relations.FuzzyRelation} classes to 
 * represent and keep track of set of scored objects and relations. While useful,
 * this API had some important drawbacks, the main one being that algorithm 
 * implementations based on FuzzySet and FuzzyRelation are very hard to 
 * read, since they require clients to use concepts such as FuzzySetElement (an 
 * element with a score) and Element (a wrapper around the object for which you 
 * want to have a collection). 
 * 
 * At the same time, FuzzySet and FuzzyRelation do not use Java's generics to 
 * inform clients what are the types of the objects contained by the collections,
 * which means developers need to rely on good documentation to inform users of 
 * the actual contents of a given Fuzzy collection. Without the type information
 * provided by generics the compiler is not able to check the correctness of 
 * algorithms (or at least that the assumptions regarding what is contained by
 * the collections is correct). One important issue with this is that refactoring
 * is much harder to perform.  
 * 
 * As java collections, Fuzzy collections are modifiable, and thus rely on clients
 * not to change the contents of collections in <i>unintended</i> ways.
 * 
 * Finally, the fuzzy collections did not provide a mechanism for tracking 
 * provenance or justifications for the scores.
 * 
 * This package is thus the result of trying to overcome the various issues with
 * Java, Guava and fuzzy collections for keeping track and managing scored 
 * (annotation) objects.
 * 
 * <h3>Overview</h3>
 * 
 * First, the package provides a basic {@link eu.xlime.util.score.Score} 
 * interface which is just a double precision score with an optional 
 * justification. The {@link eu.xlime.util.score.Score} interface defines the 
 * equality semantics between scores. The package provides a
 * default implementation {@link eu.xlime.util.score.ScoreImpl} and a factory 
 * {@link eu.xlime.util.score.ScoreFactory} class. 
 * 
 * Next, the package provides a {@link eu.xlime.util.score.Scored} interface, 
 * which basically a {@link eu.xlime.util.score.Score} linked to a scored object.
 * {@link eu.xlime.util.score.Scored} is generic in order to be able to keep 
 * track of the scored object type.
 * 
 * After that, the package provides a main collection type: the 
 * {@link eu.xlime.util.score.ScoredSet}. An immutable set of objects, each of
 * which is bound to a score. Since the collection is immutable, it provides a 
 * {@link eu.xlime.util.score.ScoredSet.Builder} which provides methods with 
 * defined semantics for adding new objects to the set. It also provides methods 
 * for normalizing the scores for the set before building it.  
 * 
 * Next, the package provides a special collection type for <i>relation-like</i>
 * objects called {@link eu.xlime.util.score.ScoredRelSet}. This allows you to 
 * score a relation between two objects (and to keep a set of such relations). 
 * The interface is defined in such a way that a relation is anything that 
 * implements the {@link eu.xlime.util.score.Tuple2} interface. As 
 * {@link eu.xlime.util.score.ScoredSet}, the scored rel set is immutable, so 
 * it provides a {@link eu.xlime.util.score.ScoredRelSet.Builder} of its own.
 *  
 * While ScoredSet only has one parameter type, ScoredRelSet has 3, one for the
 * relation, one for the <i>source</i> objects and one for the <i>destination</i> 
 * objects. This means that it is a bit painful to use as it results in long 
 * declarations. For this reason the package also provides a generic 
 * implementation called {@link eu.xlime.util.score.relation.Relations}, which 
 * has only two parameters for the source and destination objects. You have two 
 * main options for defining your own relation sets: 
 * <ol>
 * <li>Define your own by:
 *   <ul>
 *   	<li>Defining a class implementing the {@link eu.xlime.util.score.Tuple2}
 *   	interface</li>
 *   	<li>Defining a {@link eu.xlime.util.score.Tuple2.Tuple2Metadata} for this
 *   	new class</li>
 *   	<li>Defining a {@link eu.xlime.util.score.Tuple2.Builder} for this new 
 *   	class</li>
 *   	<li>Defining a class extending the default implementation for 
 *   {@link eu.xlime.util.score.ScoredRelSet}: {@link eu.xlime.util.score.ScoredRelSetImpl}.
 *   	</li>
 *   	<li>Defining a {@link eu.xlime.util.score.ScoredRelSet.Builder} for the 
 *   	new ScoredRelSet implementation, typically by overriding the builder in 
 *   	{@link eu.xlime.util.score.ScoredRelSetImpl.ScoredRelSetImplBuilder}.</li>
 *   </ul>
 *   This approach gives you full freedom to define specific semantics for your 
 *   relation objects.
 * <li>
 * <li>Reuse the {@link eu.xlime.util.score.relation.Relations} implementation by
 *   <ul>
 *   	<li>defining a {@link eu.xlime.util.score.relation.Relation.Metadata} 
 *   	instance for the type of relations you are interested in.</li>
 *   	<li>using the provided Relations builder</li>
 *   </ul>
 *   This approach is quicker to implement, but provides you with less control 
 *   about the actual {@link eu.xlime.util.score.relation.Relation} objects 
 *   contained by the built {@link eu.xlime.util.score.relation.Relations}.
 *  </li>
 * </ol>
 *   
 */
package eu.xlime.util.score;

