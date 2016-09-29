package eu.xlime.sphere.bean;

import java.io.Serializable;
import java.util.List;

import eu.xlime.bean.XLiMeResource;

/**
 * Provides a list of {@link XLiMeResource} recommendations subdivided in three 
 * <i>spheres</i>. Typically, a recommender service will generate this {@link Spheres} 
 * based on a list of input resources. In such cases, the {@link #inner} sphere will 
 * typically contain those contextual resources, while the {@link #inter}mediate and 
 * {@link #outer} spheres will respectively contain the <i>best</i> and <i>good</i> 
 * recommendations.   
 * 
 * @author RDENAUX
 *
 */
public class Spheres implements Serializable {

	private static final long serialVersionUID = 4151133154497014201L;
	
	private String name;
	private String type;
	private String uri;
	private List<Recommendation> inner;
	private List<Recommendation> inter;
	private List<Recommendation> outer;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public List<Recommendation> getInner() {
		return inner;
	}
	public void setInner(List<Recommendation> inner) {
		this.inner = inner;
	}
	public List<Recommendation> getInter() {
		return inter;
	}
	public void setInter(List<Recommendation> inter) {
		this.inter = inter;
	}
	public List<Recommendation> getOuter() {
		return outer;
	}
	public void setOuter(List<Recommendation> outer) {
		this.outer = outer;
	}
	@Override
	public String toString() {
		final int maxLen = 5;
		return String
				.format("Spheres [name=%s, type=%s, uri=%s, inner(%s)=%s, inter(%s)=%s, outer(%s)=%s]",
						name,
						type,
						uri,
						inner != null ? inner.size() : 0,
						inner != null ? inner.subList(0,
								Math.min(inner.size(), maxLen)) : null,
						inter != null ? inter.size() : 0,
						inter != null ? inter.subList(0,
								Math.min(inter.size(), maxLen)) : null,
						outer != null ? outer.size() : 0,
						outer != null ? outer.subList(0,
								Math.min(outer.size(), maxLen)) : null);
	}
	
	
}
