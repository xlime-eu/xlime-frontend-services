package eu.xlime.sparql;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.DirectTypeHierarchyInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.io.Files;

public class VoiDManagerImplTest {

	private static final Logger log = LoggerFactory.getLogger(VoiDManagerImplTest.class);

	private static final String soccerClubUri = "http://dbpedia.org/ontology/SoccerClub";
	private static final String soccerPlayerUri = "http://dbpedia.org/ontology/SoccerPlayer";
	private static final String soccerManagerUri = "http://dbpedia.org/ontology/SoccerManager";	

	private static final String directTypeUri = "http://www.openrdf.org/schema/sesame#directType";	
	private static final String managerUri = "http://dbpedia.org/property/manager";
	private static final String currentClubUri = "http://dbpedia.org/property/currentclub";
	private static final String rdfslabelUri = "http://www.w3.org/2000/01/rdf-schema#label";	
	private static final String rdfTypeUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";	
	
	static VoiDManagerImpl testObjSpanishLiga;

	@BeforeClass
	public static void setup() throws Exception {
		testObjSpanishLiga = new VoiDManagerImpl(new OpenRDFSparqlClientImpl(
				createLocalRepoFromFile(
					"src/test/resources/SpanishLiga.rdf", RDFFormat.RDFXML), 100));
	}
	
	static private Repository createLocalRepoFromFile(String localFile,
			RDFFormat fileFormat) throws RepositoryException, RDFParseException, IOException {
		return createRepoFromFile(
				new DirectTypeHierarchyInferencer(new MemoryStore()), 
				localFile, fileFormat);		
	}
	
	static private final Repository createRepoFromFile(Sail sail, String localFile,
			RDFFormat fileFormat) throws RepositoryException, RDFParseException, IOException {
		Repository repo = new SailRepository(sail);
		repo.initialize();
		
		log.debug("Loading dataset " + localFile + " into new in-memory store.");
		loadRdfFile(repo, localFile, fileFormat);
		log.debug("Loaded dataset " + localFile + " into new im-memory store with direct type inferencing.");

		return repo;
	}
	
	static private void loadRdfFile(Repository repo, String localFile,
			RDFFormat fileFormat) throws RepositoryException, RDFParseException, IOException {
		File file = new File(localFile);
		String baseURI = "http://example.com/example/local";

		RepositoryConnection con = repo.getConnection();
		try {
			con.add(Files.newReader(file, Charset.forName("utf-8")), baseURI, fileFormat);
		} finally {
			con.close();
		}
	}
	
	
	@Test
	public void testGetNumberOfTriples() throws Exception {
		assertEquals(2049, testObjSpanishLiga.getNumberOfTriples());
	}

	@Test
	public void testGetNumberOfEntities() throws Exception {
		assertEquals(421, testObjSpanishLiga.getNumberOfEntities());
	}

	@Test
	public void testGetNumberOfClasses() throws Exception {
		assertEquals(3, testObjSpanishLiga.getNumberOfClasses());
	}

	@Test
	public void testGetNumberOfProperties() throws Exception {
		assertEquals(5, testObjSpanishLiga.getNumberOfProperties());
	}

	@Test
	public void testGetDistinctSubjects() throws Exception {
		assertEquals(427, testObjSpanishLiga.getDistinctSubjects());
	}

	@Test
	public void testGetDistinctObjects() throws Exception {
		assertEquals(42, testObjSpanishLiga.getDistinctObjects());
	}

	@Test
	public void testGetClassesByInstanceCount() throws Exception {
		Multiset<URI> clsByIndCnt = testObjSpanishLiga.getClassesByInstanceCount();
		Multiset<URI> expected = ImmutableMultiset.<URI>builder()
			.setCount(URI.create(soccerClubUri), 18)
			.setCount(URI.create(soccerPlayerUri), 382)
			.setCount(URI.create(soccerManagerUri), 21).build();
		assertEquals(expected, clsByIndCnt);
	}

	@Test
	public void testGetPropertiesByTripleCount() throws Exception {
		Multiset<URI> propsByTrips = testObjSpanishLiga.getPropertiesByTripleCount(); 
		Multiset<URI> expected = ImmutableMultiset.<URI>builder()
			.setCount(URI.create(directTypeUri), 421)
			.setCount(URI.create(rdfTypeUri), 421)				
			.setCount(URI.create(rdfslabelUri), 800)							
			.setCount(URI.create(currentClubUri), 386)
			.setCount(URI.create(managerUri), 21).build();
		assertEquals(expected, propsByTrips);
	}
	
}
