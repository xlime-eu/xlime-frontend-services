package eu.xlime.sparql;

import java.net.URI;

import com.google.common.collect.Multiset;

/**
 * Defines the methods that a class that acts as a VoiDManager (i.e. that can
 * extract or read voiD metrics for a dataset/ontology) should provide.
 *
 * @author rdenaux
 */
public interface VoiDManager {

	/**
	 * Total number of triples in a given dataset/ontology
	 *
	 * @return a long.
	 */
	long getNumberOfTriples();
	
	/**
	 * total number of entities (i.e. instances of some class)
	 *
	 * @return a long.
	 */
	long getNumberOfEntities();
	
	/**
	 * total number of distinct classes for which at least one instance has
	 * been defined. Note that instances of owl:Class are ignored unless the
	 * dataset under inspection contains instances of that class.
	 *
	 * @return a long.
	 */
	long getNumberOfClasses();
	
	/**
	 * total number of distinct predicates used in some triple. Note that
	 * properties declared as instances of an rdfs or owl property type are
	 * ignored unless the predicate is actually used in a triple to relate some
	 * subject to some object.
	 *
	 * @return a long.
	 */
	long getNumberOfProperties();
	
	/**
	 * total number of distinct subject nodes
	 *
	 * @return a long.
	 */
	long getDistinctSubjects();
	
	/**
	 * total number of distinct object nodes
	 *
	 * @return a long.
	 */
	long getDistinctObjects();

	/**
	 * histogram of class {@link java.net.URI}s vs. total number of instances of the class
	 *
	 * @return a {@link com.google.common.collect.Multiset} object.
	 */
	Multiset<URI> getClassesByInstanceCount();
	
	/**
	 * histogram of property {@link java.net.URI}s vs. total number of triples using the property
	 *
	 * @return a {@link com.google.common.collect.Multiset} object.
	 */
	Multiset<URI> getPropertiesByTripleCount();
}

