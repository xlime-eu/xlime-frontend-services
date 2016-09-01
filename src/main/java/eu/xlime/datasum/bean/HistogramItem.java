package eu.xlime.datasum.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO Class: Histogram items
 * 
 * @author Nuria Garcia
 * {@link}: ngarcia@expertsystem.com
 *
 */

@XmlRootElement
public class HistogramItem implements Serializable {
	
	private static final long serialVersionUID = 851141885382491099L;
	private String item;
	private Long count;	
	
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	@Override
	public String toString() {
		return String.format("HistogramItem [item=%s, count=%s]", item, count);
	}
	
}
