package eu.xlime.bean;

/**
 * Custom information about a {@link TVProgramBean} that is only relevant for tv programs that 
 * are provided by Zattoo.
 * 
 * @author rdenaux
 *
 */
public class ZattooCustomTVInfo implements CustomTVInfo {

	private static final long serialVersionUID = -816507670344612214L;
	
	private long productionProgId;
	private String channelId;
	public final long getProductionProgId() {
		return productionProgId;
	}
	public final void setProductionProgId(long productionProgId) {
		this.productionProgId = productionProgId;
	}
	public final String getChannelId() {
		return channelId;
	}
	public final void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	@Override
	public String toString() {
		return String.format(
				"ZattooCustomTVInfo [productionProgId=%s, channelId=%s]",
				productionProgId, channelId);
	}
		
}
