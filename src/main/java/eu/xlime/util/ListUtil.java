package eu.xlime.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides convenience methods for handling {@link List}s, such as:
 * <ul>
 * <li>splitting a {@link List} into a {@link List} of sublists 
 * 	{@link #splitIntoSubListsWithMaxSize(int, List)} and {@link #splitIntoNSubLists(int, List)}</li>
 * <li>retrieving the {@link #tail(List)} of a list</li>
 * <li>weaving </li>
 * </ul> 
 * lists.
 * 
 * @author rdenaux
 *
 */
public class ListUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ListUtil.class);
	
	/**
	 * Returns the tail of an input list.
	 * 
	 * @param aList
	 * @return
	 */
	static public <T> List<T> tail(List<T> aList) {
		if (aList.size() <= 1) return Collections.emptyList();
		List<T> result = new ArrayList<T>();
		for (int i = 1; i < aList.size(); i++) {
			result.add(aList.get(i));
		}
		return result;
	}	
	
	/**
	 * Weaves various lists into a single list maintaining the order of the original lists. E.g.
	 * <pre>
	 *   weave(List("a1", "a2"), List("b1", "b2"), List("c1", "c2"); 
	 * </pre>
	 * should result in 
	 * <pre>
	 *   List("a1, "b1", "c1", "a2", "b2", "c2");
	 * </pre>
	 * @param lists
	 * @return
	 */
	public <T> List<T> weave(List<? extends T>... lists) {
		List<T> result = new ArrayList<>();
		int max = 0;
		for (List<?> list: lists) {
			log.debug("Weaving list with " + list.size() + " elements");
			max = Math.max(max, list.size());
		}
		for (int i = 0; i < max; i++) {
			for (List<? extends T> list: lists) {
				if (list.size() > i) result.add(list.get(i));
			}
		}
		log.debug("weaved list with " + result.size() + " elements");
		return result;
	}
	
	
	/**
	 * Splits a fullList into 1 or more sublists such that all the sublists are
	 * either maxSize or (the last sublist) smaller than maxSize.
	 * 
	 * @param maxSize
	 * @param fullList
	 * @return
	 */
	public <T> List<List<T>> splitIntoSubListsWithMaxSize(int maxSize, List<T> fullList) {
		assert(maxSize > 0);
		return applySplit(
				splitIntoSpansWithMaxSize(maxSize, fullList.size()),
				fullList);
	}
	
	/**
	 * Splits a fullList into a target number of n subLists.
	 * 
	 * @param n the target number of sublists, n should be between 0 and the 
	 * 	size of the fullList. 
	 * @param fullList
	 * @return
	 */
	public <T> List<List<T>> splitIntoNSubLists(int n, List<T> fullList) {
		assert(n > 0);
		assert(n < fullList.size()): "Cannot split " + fullList.size() + 
			" into " + n + " sublists.";
		return applySplit(splitIntoNSpans(n, fullList.size()), fullList);
	}
	
	/**
	 * Splits a fullList according to a split specification that consists of a
	 * list of {@link Span}s.
	 * 
	 * @param splitSpec
	 * @param fullList
	 * @return
	 */
	private <T> List<List<T>> applySplit(List<Span> splitSpec, List<T> fullList) {
		List<List<T>> result = new ArrayList<List<T>>();
		for (Span span: splitSpec) {
			result.add(fullList.subList(span.begin, span.end));
		}
		return result;
	}
	
	class Span {
		final int begin;
		final int end;
		Span(int b, int e) {
			assert(b < e);
			begin = b;
			end = e;
		}
	}
	
	/**
	 * Creates a List of size n of {@link Span}s that cover the
	 * space between 0 and fullSize. I.e. the spans in the list do not overlap 
	 * each other and the sum of sizes of all the pairs is equal to fullSize.
	 * 
	 * @param n
	 * @param fullSize
	 * @return
	 */
	private List<Span> splitIntoNSpans(int n, int fullSize) {
		assert(n > 0);
		assert(n < fullSize -1);
		List<Span> result = new ArrayList<Span>();
		int segmentSize = fullSize/n;
		for (int i = 0; i < n; i++) {
			int begin = i * segmentSize;
			int end = (i + 1) * segmentSize;
			if (end > fullSize - 1) end = fullSize - 1;
			result.add(new Span(begin, end));
		}
		return result;
	}

	/**
	 * Creates a list of one or more {@link Span}s that cover the 
	 * fullSize, while keeping the size of the spans close to (but under) the 
	 * maxSize.
	 * 
	 * The spans in the returned list do not overlap 
	 * each other and the sum of sizes of all the pairs is equal to fullSize.
	 * 
	 * @param maxSize
	 * @param fullSize
	 * @return
	 */
	private List<Span> splitIntoSpansWithMaxSize(int maxSize, int fullSize) {
		assert(maxSize > 0);
		List<Span> result = new ArrayList<Span>();
		int end = 0;
		while (end < fullSize - 1) {
			int begin = result.size() * maxSize;
			end = maxSize * (result.size() + 1);
			if (end > fullSize - 1) end = fullSize - 1;
			result.add(new Span(begin, end));
		}
		return result;
	}

}
