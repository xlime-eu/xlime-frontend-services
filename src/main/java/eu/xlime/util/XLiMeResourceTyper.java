package eu.xlime.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import eu.xlime.bean.XLiMeResource;

public class XLiMeResourceTyper {

	/**
	 * Returns the canonical {@link XLiMeResource} class for a given bean (i.e. not a particular implementation).
	 * 
	 * @param bean
	 * @return
	 */
	public <T extends XLiMeResource> Class<T> findResourceClass(XLiMeResource bean) {
		return (Class<T>) bean.getClass(); //TODO: reimplement when needed
	}
	
	public Set<Class<? extends XLiMeResource>> findResourceClasses(Collection<? extends XLiMeResource> beans) {
		Set<Class<? extends XLiMeResource>> result = new HashSet<>();
		for (XLiMeResource bean: beans) {
			result.add(findResourceClass(bean));
		}
		return result;
	}
	
	public <T extends XLiMeResource> List<T> filterByType(Class<T> clz, List<? extends XLiMeResource> beans) {
		List<T> result = new ArrayList<>();
		for (XLiMeResource res: beans) {
			if (clz.equals(findResourceClass(res))) result.add((T)res);
		}
		return ImmutableList.copyOf(result);
	}
}
