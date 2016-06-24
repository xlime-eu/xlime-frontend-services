package eu.xlime.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xlime.bean.UIDate;

public class SparqlToBeanConverter {

	private static final Logger log = LoggerFactory.getLogger(SparqlToBeanConverter.class);
	
	public UIDate asUIDate(Date aDate) {
		return new UIDate(aDate);
	}
	
	public final Date extractISODate(String dateTime) {
		if (dateTime == null || dateTime.isEmpty()) return null;
		DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
		try {
			return parser.parseDateTime(dateTime).toDate();
		} catch (IllegalArgumentException e) {
			log.trace("Found invalid ISODate " + dateTime + " Attempting failback format..");
			return failbackDateNoTimezone(dateTime);
		}
	}
	
	private Date failbackDateNoTimezone(String dateTime) {
		SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY);
		try {
			return ISO8601DATEFORMAT.parse(dateTime);
		} catch (ParseException e) {
			throw new RuntimeException("Error parsing " + dateTime, e);
		}
	}
	
}
