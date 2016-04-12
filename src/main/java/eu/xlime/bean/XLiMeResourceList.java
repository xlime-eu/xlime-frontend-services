package eu.xlime.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "resources")
public class XLiMeResourceList {

	private List<XLiMeResource> resources = new ArrayList<>();
	
	private List<String> errors = new ArrayList<>();

	public List<XLiMeResource> getResources() {
		return resources;
	}

	public void setResources(List<XLiMeResource> resources) {
		this.resources = resources;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
}
