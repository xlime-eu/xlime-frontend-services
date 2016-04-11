package eu.xlime.bean;

import java.io.Serializable;

public class Content implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 3836137457813363360L;

	/**
	 * The full content (may not be null)
	 */
	private String full;
	
	/**
	 * A pre-view of the {@link #full} content, this will generally be capped 
	 * to a certain amount of characters. However, if the {@link #full} content 
	 * is already smaller than this limit, {@link #preview} may be <code>null</code>.  
	 */
	private String preview;
	
	public Content() {
		
	}

	public String getFull() {
		return full;
	}

	public void setFull(String full) {
		this.full = full;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}
	
}
