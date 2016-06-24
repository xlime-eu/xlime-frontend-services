package eu.xlime.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ontopia.utils.CompactHashSet;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.apache.lucene.util.fst.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;

public class FstKBEntityMapper implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1975821689943770937L;

	private static final Logger log = LoggerFactory.getLogger(FstKBEntityMapper.class);

	/**
	 * {@link FST} output type used for the token-, synonym- and name 
	 * dictionaries.
	 */
	private static final PositiveIntOutputs fstOutputs = PositiveIntOutputs.getSingleton();

	/**
	 * Provides a list of url segments which should be included when loading sameAs files (this helps to reduce the overall size 
	 * of the dictionaries)
	 */
	private final List<String> supportedOtherDomains;

	/**
	 * The rdf file path from which this {@link FstKBEntityMapper} was read (may be null, 
	 * for the empty {@link FstKBEntityMapper}). 
	 */
	protected final String sameAsTtlFilePath;
	
	/**
	 * The dictionary of <i>non-canonical</i> uris. It maps all these uris in this 
	 * {@link FstKBEntityMapper} to a unique {@link Long} ( {@link PositiveIntOutputs}, 
	 * really) id.
	 *
	 * The dictionary is an {@link FST} which has, as its inputs, URIs 
	 * (as {@link BytesRef}).
	 */
	private transient FST<Long> otherUriDict;
	
	/**
	 * The dictionary of <i>canonical</i> uris. It maps all the canonical Uris in this 
	 * {@link FstKBEntityMapper} to a unique {@link Long} ( {@link PositiveIntOutputs}, 
	 * really) id.
	 * 
	 * The user needs to define which are the <b>Canonical Uris</b>, typically those coming from 
	 * some chosen scheme to be used as the canonical ones. E.g. the English DBpedia Urls as opposed to the language-dependent 
	 * dbpedia urls (which would be stored in the {@link #otherUriDict}.
	 * 
	 * The dictionary is an {@link FST} which has, as its inputs, URIs 
	 * (as {@link BytesRef}).
	 */
	private transient FST<Long> canonUriDict; //Input a String (output, a positive int
	
	/**
	 * Index for finding canonical uris from some other uri. This is 
	 * done by mapping non-canonical uri ids (outputs of {@link #otherUriDict} to canonica-uri
	 * ids (outputs of {@link #canonUriDict}). 
	 * 
	 * This is the inverse of {@link #othersByCanon}.
	 */
	private int[][] canonByOther;
	
	/**
	 * Index for finding non-canonical uris for a given canonical uri. This is
	 * done by mapping canonical-uri ids (i.e. outputs of {@link #canonUriDict} to 
	 * non-canonical uri ids (i.e. outputs of {@link #otherUriDict}).
	 * 
	 * This is the inverse of {@link #canonByOther}.
	 */
	private int[][] othersByCanon;
	
	abstract class BaseSameAsTtlProcessor<T> {
		
		public BaseSameAsTtlProcessor() {
		}
		
		protected final T processSameAsFile(File sameAsFile) {
			final long start = System.currentTimeMillis();
			try {
				return Files.readLines(sameAsFile, Charsets.UTF_8, new LineProcessor<T>(){
					private long currLine = 0;
					private long triplesParsed = 0;
					private long triplesProcessed = 0;				

					@Override
					public boolean processLine(String line) throws IOException {
						currLine++;
						printProgress();
						if (line.startsWith("#")) return true;
						Optional<Triple> optT = parseTriple(line);
						if (optT.isPresent()) {
							triplesParsed++;
							Triple t = optT.get();
							assert(t.getPredicate().hasURI("http://www.w3.org/2002/07/owl#sameAs"));
							if (processSameAsTriple(t)) triplesProcessed++;
						}
						return true;
					}

					private void printProgress() {
						if (currLine % 1000 != 0)  return;
						double seconds = (double)(System.currentTimeMillis() - start) / 1000.0;
						System.out.println(String.format("Line %s, triplesRead %s, triplesProcessed %s, speed %s lines/sec", 
								currLine, triplesParsed, triplesProcessed, currLine/seconds));
					}
					
					private Optional<Triple> parseTriple(String line) {
						try {
							Optional<Dataset> ds = parse(line.getBytes(), Lang.TTL);
							if (ds.isPresent()) {
								Model model = ds.get().getDefaultModel();
								assert(model.size() == 1);
								return Optional.of(model.listStatements().next().asTriple());
							}
							return Optional.absent();
						} catch (Exception e) {
							System.err.println("Failed to parse " + line + ". " + e.getLocalizedMessage());
							return Optional.absent();
						}
					}
				
					private Optional<Dataset> parse(byte[] bytes, Lang lang) {
						Dataset dataset = DatasetFactory.createMem();
						try {
							InputStream stream = new ByteArrayInputStream(
									bytes);
							RDFDataMgr.read(dataset, stream, lang);
						} catch (Exception e) {
							System.err.println("Failed to parse RDF from input stream. " + e.getLocalizedMessage());
							dataset.close();
							return Optional.absent();
						}
						return Optional.of(dataset);
					}
					
					@Override
					public T getResult() {
						return buildResult();
					}
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		protected abstract boolean processSameAsTriple(Triple triple);

		protected abstract T buildResult();
		
		public abstract T getResult();
	}
	
	class BiUris {
		Set<String> canonUris;
		Set<String> otherUris;
		
		public BiUris() {
			canonUris = new CompactHashSet<>(); //new HashSet<>();
			otherUris = new CompactHashSet<>(); //new HashSet<>();
		}
		
		public void put(String canonUri, String otherUri) {
			canonUris.add(canonUri);
			otherUris.add(otherUri);
		}
	}
	
	class UriLoader extends BaseSameAsTtlProcessor<BiUris> {

		private BiUris uris;
		
		public UriLoader(File sameAsTtlFile) {
			uris = new BiUris();
			processSameAsFile(sameAsTtlFile);
		}
		
		@Override
		protected boolean processSameAsTriple(Triple triple) {
			String canon = triple.getSubject().getURI();
			String other = triple.getObject().getURI();
			if (isSupportedOtherUrl(other)) {
				uris.put(canon, other);
				return true;
			} else return false;
		}

		@Override
		protected BiUris buildResult() {
			return uris;
		}

		@Override
		public BiUris getResult() {
			return uris;
		}
		
	}
	
	class BiMap {
		public int[][] canonToOther;
		public int[][] otherToCanon;
		
		public BiMap(int canonSize, int otherSize) {
			canonToOther = new int[canonSize][];
			otherToCanon = new int[otherSize][];
		}
		
		public void put(int canonUrlId, int otherUrlId) {
			put(canonToOther, canonUrlId, otherUrlId);
			put(otherToCanon, otherUrlId, canonUrlId);
		}
		
		private void put(int[][] map, int key, int val) {
			map[key] = append(map[key], val);
		}

		private int[] append(int[] values, int val) {
			if (values == null) {
				int[] singleton = { val };
				return singleton;
			}
			int[] newArr = Arrays.copyOf(values, values.length + 1);
			newArr[values.length] = val;
			return newArr;
		}
	}
	
	class BiMapLoader extends BaseSameAsTtlProcessor<BiMap> {
		BiMap result;

		public BiMapLoader(File sameAsTtlFile, int canonSize, int otherSize){
			super();
			result = new BiMap(canonSize, otherSize);
			processSameAsFile(sameAsTtlFile);
		}
		@Override
		protected boolean processSameAsTriple(Triple triple) {
			final String canon = triple.getSubject().getURI();
			final String other = triple.getObject().getURI();
			if (isSupportedOtherUrl(other)) {
				final int cid = lookupCanonUriId(canon);
				final int oid = lookupOtherUriId(other);
				if (cid < 0) {
					log.error("Failed to lookup canonicUri " + canon);
					return false;
				}
				if (oid < 0) {
					log.error("Failed to lookup otherUri " + other);
					return false;
				}
				result.put(cid, oid);
				return true;
			} else return false;
		}

		@Override
		protected BiMap buildResult() {
			return result;
		}

		@Override
		public BiMap getResult() {
			return result;
		}
		
	}

	
	public int lookupCanonUriId(String canonUri) {
		return lookupUriInDict(canonUriDict, canonUri);
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	protected boolean isSupportedOtherUrl(String url) {
		if (supportedOtherDomains.isEmpty()) return true;
		for (String sup: supportedOtherDomains) {
			if (url.contains(sup)) return true;
		}
		return false;
	}

	private int lookupUriInDict(FST<Long> dict, String uri) {
		if (dict == null)
			return -1;
		try {
			Long val = Util.get(dict, new BytesRef(uri));
			if (val == null)
				return -1;
			return val.intValue();
		} catch (IOException e) {
			throw new RuntimeException(e);//weird
		}
	}
	
	public int lookupOtherUriId(String otherUri) {
		return lookupUriInDict(otherUriDict, otherUri);
	}
	
	
	public FstKBEntityMapper(File sameAsTtlFile, File supportedOtherUrisFile) {
		log.debug("Building FstKBEntityMapper");
		this.sameAsTtlFilePath = sameAsTtlFile.getAbsolutePath();
//		this.supportedOtherUrisFile = supportedOtherUrisFile.getAbsolutePath();
		this.supportedOtherDomains = readSupportedOtherDomains(supportedOtherUrisFile);
		
		if (initFromSerializationForFile(sameAsTtlFile, supportedOtherUrisFile)) {
			if (log.isDebugEnabled()) {
				printDebugInfo();
			}
			return;
		}
		
		try {
			BiUris biUris = new UriLoader(sameAsTtlFile).getResult();
			final int otherSize = biUris.otherUris.size();
			otherUriDict = buildUriFST(biUris.otherUris);
			
			final int canonSize = biUris.canonUris.size();
			canonUriDict = buildUriFST(biUris.canonUris);
			biUris = null; //GC

			BiMap biMap = new BiMapLoader(sameAsTtlFile, canonSize, otherSize).getResult();
			canonByOther = biMap.otherToCanon;
			othersByCanon = biMap.canonToOther;
			
			if (log.isDebugEnabled()) {
				printDebugInfo();
			}
			saveSerializationForFile(sameAsTtlFile, supportedOtherUrisFile);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

	}
	
	private List<String> readSupportedOtherDomains(File supportedOtherUrisFile) {
		try {
			return Files.readLines(supportedOtherUrisFile, Charsets.UTF_8, new LineProcessor<List<String>>(){

				private List<String> builder = new ArrayList<String>();
				
				@Override
				public boolean processLine(String line) throws IOException {
					if (line.startsWith("#")) return true;
					builder.add(line.trim());
					return true;
				}

				@Override
				public List<String> getResult() {
					return ImmutableList.copyOf(builder);
				}});
		} catch (IOException e) {
			log.warn("Failed to load list of supported other uris, will load all.", e);
			return ImmutableList.of();
		}
	}

	public Optional<String> toCanonicalEntityUrl(String entUrl) {
		int otherUriId = lookupOtherUriId(entUrl);
		if (otherUriId < 0) return Optional.absent();
		int[] canonicalIds = canonByOther[otherUriId];
		if (canonicalIds.length > 1) {
			log.debug(String.format("Multiple %s canonical entities for %s: %s", canonicalIds.length, entUrl, toCanonicalUris(canonicalIds)));
		}
		try {
			return Optional.of(lookupCanonUri(canonicalIds[0]));
		} catch (IOException e) {
			throw new RuntimeException("Error retrieving canonUri.", e);
		}
	}
	

	private List<String> toCanonicalUris(int[] canonUriIds) {
		List<String> result = new ArrayList<>();
		for (int id : canonUriIds){
			try {
				result.add(lookupCanonUri(id));
			} catch (IOException e) {
				throw new RuntimeException("Error retrieving canonUri.", e);
			}
		}
		return result;
	}
	
	public Set<String> expandSameAs(String entUrl) {
		int canonUriId = lookupCanonUriId(entUrl);
		if (canonUriId < 0) {
			//entUrl is not canonical, try to find canonical and expand that
			Optional<String> optCanonUri = toCanonicalEntityUrl(entUrl);
			if (optCanonUri.isPresent())
				return expandSameAs(optCanonUri.get());
			else return ImmutableSet.of();
		} else { //expand canonical
			int[] otherIds = othersByCanon[canonUriId];
			return ImmutableSet.copyOf(lookupOtherIds(otherIds));
		}
	}
	
	private List<String> lookupOtherIds(int[] otherIds) {
		List<String> result = new ArrayList<>();
		for (int id : otherIds){
			try {
				result.add(lookupOtherUri(id));
			} catch (IOException e) {
				throw new RuntimeException("Error retrieving otherUri.", e);
			}
		}
		return result;
	}

	private String lookupCanonUri(int id) throws IOException {
		IntsRef wordIntsRef = Util.getByOutput(canonUriDict, id);
		return Util.toBytesRef(wordIntsRef, new BytesRefBuilder()).utf8ToString();
	}
	
	private String lookupOtherUri(int id) throws IOException {
		IntsRef wordIntsRef = Util.getByOutput(otherUriDict, id);
		return Util.toBytesRef(wordIntsRef, new BytesRefBuilder()).utf8ToString();
	}

	private FST<Long> buildUriFST(Collection<String> otherUris) throws IOException {
		log.debug("Building name dict FST with " + otherUris.size() + " uris...");
		//shareOutputs=true so we can do reverse lookup (lookup by ord id)
		Builder<Long> builder = new Builder<Long>(FST.INPUT_TYPE.BYTE1, fstOutputs);
		
		String[] uris = otherUris.toArray(new String[otherUris.size()]);
		Arrays.sort(uris);
		
		long dictId = 0;
		IntsRefBuilder scratchInts = new IntsRefBuilder();
		for (String uri: uris) {
			BytesRef uriBytesRef = new BytesRef(uri);
			builder.add(Util.toIntsRef(uriBytesRef, scratchInts), dictId++);
		}
		return builder.finish();
	}

	

	/**
	 * Tries to initialize this {@link FstKBEntityMapper} by deserializing a 
	 * previously serialized file representing the same information as that 
	 * contained by thesaurusFile.
	 *  
	 * @param interlangTtlFile a <b>normal</b> .ttl file containing <code>owl:sameAs</code> 
	 * 	triples.
	 * @return <code>true</code> if there was a serialized version of 
	 * 	interlangTtlFile on disk (on the correct location) which could be 
	 * 	deserialized. <code>false</code>, otherwise (i.e. could not initialize 	
	 * 	this {@link FstKBEntityMapper}). 
	 */
	private boolean initFromSerializationForFile(File interlangTtlFile, File supportedOtherUriFile) {
		if (interlangTtlFile.getAbsolutePath().endsWith(".ttl")) {
			String thesFstPath;
			try {
				thesFstPath = fstFilePathFromThesFile(interlangTtlFile, supportedOtherUriFile);
			} catch (IOException e1) {
				if (log.isDebugEnabled()) {
					log.debug("Error detecting path for fst serialization of " 
							+ interlangTtlFile.getAbsolutePath() + ". " 
							+ e1.getLocalizedMessage() 
							+ ". Using original file instead (slower).");
					e1.printStackTrace();
				}
				return false;
			}
			File savedFstFile = new File(thesFstPath);
			try {
				if (savedFstFile.exists()) {
					FstKBEntityMapper copy = FstKBEntityMapper.load(savedFstFile);
					otherUriDict = copy.otherUriDict;
					canonUriDict = copy.canonUriDict;
					canonByOther = copy.canonByOther;
					othersByCanon = copy.othersByCanon;
					log.debug("Loaded FSTKBEntityMapper from previously saved FST file " + savedFstFile);
					return true;
				}
			} catch (IOException e) {
				//error deserializing file, ignore and
				if (log.isDebugEnabled()) {
					log.debug("Error deserializing FstKBEntityMapper from " + 
						savedFstFile.getAbsolutePath() + ". " + 
						e.getLocalizedMessage() + ". Will load from original file instead.");
					if (log.isTraceEnabled())
						e.printStackTrace();
				}
				return false;
			}
		}
		return false;
	}
	
	private void saveSerializationForFile(File sameAsTtlFile, File supportedOtherUriFile) throws IOException {
		if (sameAsTtlFile.getAbsolutePath().endsWith(".ttl")) {
			long beforeSaveFst = System.currentTimeMillis();
			String newThesFstPath = fstFilePathFromThesFile(sameAsTtlFile, supportedOtherUriFile);
			try {
				save(new File(newThesFstPath));
			} catch (IOException ioe) {
				ioe.printStackTrace();
				log.debug("Error saving FSTThesaurus to disk:  " + ioe.getLocalizedMessage());
			}
			long afterSaveFst = System.currentTimeMillis();
			long timeSavingFst = afterSaveFst - beforeSaveFst;
			log.debug("Saved FSTThesaurus to " + newThesFstPath + " in " + timeSavingFst + "ms");
		}
	}
	
	private String fstFilePathFromThesFile(File sameAsTtlFile, File supportedOtherUriFile) throws IOException {
		HashCode md5 = Files.hash(sameAsTtlFile, Hashing.md5());
		String md5Hex = md5.toString();
		HashCode supMd5 = Files.hash(supportedOtherUriFile, Hashing.md5());
		String supMd5Hex = supMd5.toString();
		String thesPath = sameAsTtlFile.getAbsolutePath();
		String newThesFstPath = String.format("%s-%s-%s.ttl.fst", thesPath.substring(0, 
				thesPath.length() - ".ttl".length()), md5Hex.substring(0, 8), supMd5Hex.substring(0, 8));
		return newThesFstPath;
	}	

	public void printDebugInfo() {
		if (otherUriDict == null) {
			log.debug("Empty FSTs");
			return;
		}
		log.debug("Built FST " + (otherUriDict.getArcWithOutputCount() + 1) + " non-canon-uris (" + otherUriDict.ramBytesUsed() / 1024 + "kb).");
		log.debug("Built FST " + (canonUriDict.getArcWithOutputCount() + 1) + " canon-uris(" + canonUriDict.ramBytesUsed() / 1024 + "kb).");		
//		int idsBytes = (docIdsHeap.length + docIdsLookup.length) * 4;
//		log.info("Hold heap of " + docIdsHeap.length + " docIds consuming " + (idsBytes / 1024) + "kb).");
	}

	// ************** SAVE & LOAD *************

	public void save(File file) throws IOException {
		log.debug("Saving " + file);
		ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		try {
			outputStream.writeObject(this);
		} finally {
			outputStream.close();
		}
	}

	public static FstKBEntityMapper load(File file) throws IOException {
		log.debug("Loading "+file);
		ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
		try {
			return (FstKBEntityMapper) inputStream.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e.toString(),e);
		} finally {
			inputStream.close();
		}
	}

	/** required for Serializable */
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		outputStream.defaultWriteObject();
		OutputStreamDataOutput outputStreamDataOutput = new OutputStreamDataOutput(outputStream);
		if (otherUriDict != null) {
			otherUriDict.save(outputStreamDataOutput);
			canonUriDict.save(outputStreamDataOutput);
		}
	}

	/** required for Serializable */
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		InputStreamDataInput inputStreamDataInput = new InputStreamDataInput(inputStream);
		//TODO what if there is no dict/fst (they are empty?)
		otherUriDict = new FST<Long>(inputStreamDataInput, fstOutputs);
		canonUriDict = new FST<Long>(inputStreamDataInput, fstOutputs);
	}
	
}
