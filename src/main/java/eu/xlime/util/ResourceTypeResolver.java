package eu.xlime.util;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import net.expertsystem.zapi.ZattooChannelIdMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import eu.xlime.Config;
import eu.xlime.bean.ASRAnnotation;
import eu.xlime.bean.EREvent;
import eu.xlime.bean.MicroPostBean;
import eu.xlime.bean.NewsArticleBean;
import eu.xlime.bean.OCRAnnotation;
import eu.xlime.bean.SearchString;
import eu.xlime.bean.SubtitleSegment;
import eu.xlime.bean.TVProgramBean;
import eu.xlime.bean.VideoSegment;
import eu.xlime.bean.XLiMeResource;
import eu.xlime.bean.ZattooStreamPosition;
import eu.xlime.dao.SearchStringFactory;
import eu.xlime.prov.bean.ProvActivity;
import eu.xlime.summa.bean.UIEntity;

/**
 * Provides methods for determining the type of an {@link XLiMeResource} based on its URI
 * 
 * @author RDENAUX
 *
 */
public class ResourceTypeResolver {

	private static final Logger log = LoggerFactory.getLogger(ResourceTypeResolver.class);
	
	public Class<? extends XLiMeResource> resolveType(String uri) {
		if (isNewsArticle(uri)) return NewsArticleBean.class;
		if (isMicroPost(uri)) return MicroPostBean.class;
		if (isTVProgram(uri)) return TVProgramBean.class;
		if (isKBEntity(uri)) return UIEntity.class;
		if (isEREvent(uri)) return EREvent.class;
		if (isASRAnnotation(uri)) return ASRAnnotation.class;
		if (isOCRAnnotation(uri)) return OCRAnnotation.class;
		if (isVideoSegment(uri)) return VideoSegment.class;
		if (isSearchString(uri)) return SearchString.class;
		if (isSubtitleSegment(uri)) return SubtitleSegment.class;
		if (isProvActivity(uri)) return ProvActivity.class;
		throw new RuntimeException("Could not determine xLiMe Resource type for " + uri);
	}
	
	private boolean isProvActivity(String uri) {
		return uri.matches("http://xlime.eu/activity/*");
	}

	private boolean isSubtitleSegment(String uri) {
		// subtitles for zattoo program with start offset and an end offset
		return uri.matches("http://zattoo.com/program/\\d+/subtitles/\\d+/\\d+"); 
	}
	
	public String calcUrl(SubtitleSegment stSeg, String subtitleTrackUrl) {
		if (stSeg.getPartOf().getPosition() instanceof ZattooStreamPosition) {
			long startTime = ((ZattooStreamPosition)stSeg.getPartOf().getPosition()).getValue();
			long endTime = startTime + 40000;
			return String.format("%s/%s/%s", subtitleTrackUrl, startTime, endTime);
		} else throw new RuntimeException("Cannot coin url for " + stSeg);
	}
	
	public boolean isMediaItem(String uri) {
		return (isMicroPost(uri) || isNewsArticle(uri) || isTVProgram(uri));
	}

	public String extractSubtitleTrackUri(SubtitleSegment subtitSeg) {
		int i = subtitSeg.getUrl().indexOf("/subtitles/");
		if (i < 0) throw new RuntimeException("Unexpected subtitleSegment url" + subtitSeg);
		return subtitSeg.getUrl().substring(0, i) + "/subtitles";
	}

	public String extractAudioTrackUri(ASRAnnotation asrAnn) {
		int i = asrAnn.getUrl().indexOf("/audio/");
		if (i < 0) throw new RuntimeException("Unexpected ASRAnnotation url" + asrAnn);
		return asrAnn.getUrl().substring(0, i) + "/audio";
	}
	
	public boolean isNewsArticle(String uri) {
		return uri.startsWith("http://ijs.si/article/");
	}
	
	public boolean isMicroPost(String uri) {
		return uri.startsWith("http://vico-research.com/social/");
	}
	
	public boolean isTVProgram(String uri) {
		return uri.matches("http://zattoo.com/program/\\d+");
	}
	
	public boolean isKBEntity(String uri) {
		/* TODO: currently only support dbpedia, and only "default" dbpedia, not
		 *  any of the language dependent versions. 
		 * wikidata could also be an option, but we need a summa service for it
		 * wikipedia is not considered a KBEntity, see KBEntityMapper to go from a 
		 * 	wikipedia page to dbpedia
		 */
		return uri.startsWith("http://dbpedia.org/"); 
	}
	
	public boolean isEREvent(String uri) {
		return false; //TODO: implement
	}
	
	public boolean isASRAnnotation(String uri) {
		return uri.matches("http://zattoo.com/program/\\d+/audio/asr/\\d+/\\d+"); 
	}
	
	public String calcUrl(ASRAnnotation asrAnn, String audioTrackUrl) {
		if (asrAnn.getInSegment().getPosition() instanceof ZattooStreamPosition) {
			long startTime = ((ZattooStreamPosition)asrAnn.getInSegment().getPosition()).getValue();
			long endTime = startTime + 40000;
			//result should be something like http://zattoo.com/program/116804985/audio/asr/100290988/100330988
			return String.format("%s/asr/%s/%s", audioTrackUrl, startTime, endTime);
		} else throw new RuntimeException("Cannot coin url for " + asrAnn);
	}

	public boolean isOCRAnnotation(String uri) {
		return uri.matches("http://zattoo.com/program/\\d+/video/ocr/\\d+/\\d+"); 
	}
	
	public String calcUrl(OCRAnnotation ocrAnn, String videoTrackUrl) {
		if (ocrAnn.getInSegment().getPosition() instanceof ZattooStreamPosition) {
			long startTime = ((ZattooStreamPosition)ocrAnn.getInSegment().getPosition()).getValue();
			long endTime = startTime + 40000;
			//result should be something like http://zattoo.com/program/116804985/video/ocr/100290988/100330988
			return String.format("%s/ocr/%s/%s", videoTrackUrl, startTime, endTime);
		} else throw new RuntimeException("Cannot coin url for " + ocrAnn);
	}
	
	public boolean isVideoSegment(String uri) {
		// the program with progId, startTime and endTime offsets
		return uri.matches("http://zattoo.com/program/\\d+/\\d+/\\d+"); 
	}
	
	public boolean isSearchString(String uri) {
		return uri.startsWith(SearchStringFactory.baseUrl);
	}

	public boolean isAudioTrack(String uri) {
		return uri.matches("http://zattoo.com/program/\\d+/audio"); 
	}

	public Optional<String> audioTrackUrlAsTVProgUrl(String uri) {
		if (!isAudioTrack(uri)) throw new IllegalArgumentException("Uri is not a audio track url " + uri);
		return Optional.of(uri.replaceAll("/audio", ""));
	}
	
	public boolean isVideoTrack(String uri) {
		return uri.matches("http://zattoo.com/program/\\d+/video"); 
	}

	public Optional<String> videoTrackUrlAsTVProgUrl(String uri) {
		if (!isVideoTrack(uri)) throw new IllegalArgumentException("Uri is not a video track url " + uri);
		return Optional.of(uri.replaceAll("/video", ""));
	}
	
	public boolean isSubtitleTrack(String uri) {
		return uri.matches("http://zattoo.com/program/\\d+/subtitles"); 
	}

	public Optional<String> subtitleTrackUrlAsTVProgUrl(String uri) {
		if (!isSubtitleTrack(uri)) throw new IllegalArgumentException("Uri is not a subtitle track url " + uri);
		return Optional.of(uri.replaceAll("/subtitles", ""));
	}
	
	class ZattooTVProg {
		final TVProgramBean bean;

		final ZattooChannelIdMapper cidMapper;
		
		public ZattooTVProg(TVProgramBean bean) {
			super();
			validate(bean);
			this.bean = bean;
			Config cfg = new Config();
			try {
				cidMapper = new ZattooChannelIdMapper(new File(cfg.get(Config.Opt.ZattooChannelTitlesToIdsFile)));
			} catch (IOException e) {
				throw new RuntimeException("Cannot map cids", e);
			}
		}

		private void validate(TVProgramBean aBean) {
			if (aBean == null) throw new NullPointerException("Need valid " + TVProgramBean.class.getSimpleName());
			if (aBean.getUrl() == null) throw new NullPointerException("Invalid (null URL) " + aBean);
			if (aBean.getBroadcastDate() == null) throw new NullPointerException("Invalid (null broadcastDate): " + aBean);
			if (aBean.getDuration() == null) throw new NullPointerException("Invalid (null duration): " + aBean);
		}

		String getZattooHost() {
			//TODO: make this configuable to be able to switch to zattoo's prod env
			return "zattoo-production-zapi-sandbox.zattoo.com";
		}
		
		String getChannelId() {
			Optional<String> optCid = cidMapper.getCidForTitle(bean.getPublisher().getLabel());
			if (optCid.isPresent()) return optCid.get();
			else return "unknown-channel"; 
		}
		
		Long getProgId() {
			final String url = bean.getUrl();  
			assert(isTVProgram(url));
			return Long.parseLong(url.substring(url.lastIndexOf("/") + 1));
		}
		
		Long getStartEpoch() {
			return bean.getBroadcastDate().timestamp.getTime();
		}
		
		Long getEndEpoch() {
			return getStartEpoch() + ((long)bean.getDuration().getTotalSeconds() * 1000);
		}
	}
	
	class ZattooVidSeg {
		final VideoSegment bean;
		final ZattooTVProg prog;
		public ZattooVidSeg(VideoSegment bean) {
			super();
			this.bean = bean;
			prog = new ZattooTVProg(bean.getPartOf());
		}
		
		public ZattooTVProg getProg() {
			return prog;
		}
		
		Long getOffset() {
			if (bean.getStartTime() != null) {			
				long fix = 2L * 60L * 60L * 1000L; //startTime is 2 hours behind?
				return (bean.getStartTime().timestamp.getTime() + fix) - bean.getPartOf().getBroadcastDate().timestamp.getTime();
			}
			if (bean.getPosition() instanceof ZattooStreamPosition) {
				long seconds = ((ZattooStreamPosition)bean.getPosition()).getValue() / 4L;
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DATE, 13);
				cal.set(Calendar.MONTH, 6);
				cal.set(Calendar.YEAR, 2016);
				cal.set(Calendar.HOUR, 18);
				cal.set(Calendar.MINUTE, 2);
				cal.set(Calendar.SECOND, 7);
				cal.set(Calendar.MILLISECOND, 500);
				
				cal.add(Calendar.HOUR, (int)-(seconds / 3600));
				log.info("Start time for stream offset: " + cal + "\n\ti.e: " + cal.getTime());
				return cal.getTime().getTime();
			}
			throw new IllegalArgumentException("VideoSegment should have either a start-time or a stream position, but found " + bean);
		}
	}
	
	public String toWatchUrl(VideoSegment vidSeg) {
		ZattooVidSeg zseg = new ZattooVidSeg(vidSeg);
		return String.format("http://%s/watch/%s/%s/%s/%s/%s",
				zseg.prog.getZattooHost(), zseg.prog.getChannelId(), zseg.prog.getProgId(), zseg.prog.getStartEpoch(), zseg.prog.getEndEpoch(), zseg.getOffset());
	}
	
	public String toWatchUrl(TVProgramBean bean) {
		ZattooTVProg prog = new ZattooTVProg(bean);
		return String.format("http://%s/watch/%s/%s/%s/%s",
				prog.getZattooHost(), prog.getChannelId(), prog.getProgId(), prog.getStartEpoch(), prog.getEndEpoch());
	}
	
}
