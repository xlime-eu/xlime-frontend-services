package eu.xlime.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import eu.xlime.bean.UIDate;

public class SparqlToBeanConverter {

	private static final Logger log = LoggerFactory.getLogger(SparqlToBeanConverter.class);
	
	public UIDate asUIDate(Date aDate) {
		return new UIDate(aDate);
	}
	
	final Map<String, DateTimeFormatter> jodaTimeParsers = ImmutableMap.of(//
			"dateTime", ISODateTimeFormat.dateTime(), 
			"dateTimeNoMillis", ISODateTimeFormat.dateTimeNoMillis()
	);
	final SimpleDateFormat[] simpleDateParsers = { 
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSZ"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSZ"),			
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),			
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ"),	
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
			};
	
	public final Date extractISODate(String dateTime) {
		if (dateTime == null || dateTime.isEmpty()) return null;
		try {
			return 	extractISODate(dateTime, jodaTimeParsers);
		} catch (Exception e) {
			try {
				return extractISODate(dateTime, simpleDateParsers);
			} catch (Exception e1) {
				if (log.isTraceEnabled()) {
					log.warn("Found invalid ISODate " + dateTime + " Attempting failback format..");
				}
				return failbackDefaultTimezone(dateTime);	
			}
		}
	}
	
	private Date extractISODate(String dateTime,
			Map<String, DateTimeFormatter> parsers) {
		for (String name: parsers.keySet()) {
			DateTimeFormatter parser = parsers.get(name);
			try {
				return parser.parseDateTime(dateTime).toDate();
			} catch (IllegalArgumentException e) {
				log.trace(String.format("Failed to parse '%s' using %s", dateTime, name));
			}
		}
		throw new IllegalArgumentException("Could not parse " + dateTime + " using jodaTime parsers");
	}

	private Date extractISODate(String dateTime,
			SimpleDateFormat[] parsers) {
		for (SimpleDateFormat parser: parsers) {
			try {
				return parser.parse(dateTime);
			} catch (ParseException e) {
				log.trace(String.format("Failed to parse '%s' using %s", dateTime, parser.toPattern()));
			}
		}
		throw new IllegalArgumentException("Could not parse " + dateTime + " using simple date-formats parsers");
	}
			
	private Date failbackDefaultTimezone(String dateTime) {
		SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY);
		try {
			return ISO8601DATEFORMAT.parse(dateTime);
		} catch (ParseException e) {
			throw new RuntimeException("Error parsing " + dateTime, e);
		}
	}
	
}
