package com.angorasix.projects.core.infrastructure.persistence.converter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;


public class ZonedDateTimeConverters {
  public static final String DATE_TIME = "dateTime";
  public static final String ZONE = "zone";

  @Component
  @ReadingConverter
  public static class ZoneDateTimeReaderConverter implements Converter<Document, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(final Document source) {
      if (source == null) {
        return null;
      }


      final Date dateTime = source.getDate(DATE_TIME);
      final String zoneId = source.getString(ZONE);
      final ZoneId zone = ZoneId.of(zoneId);

      return ZonedDateTime.ofInstant(dateTime.toInstant(), zone);
    }
  }

  @Component
  @WritingConverter
  public static class ZoneDateTimeWritingConverter implements Converter<ZonedDateTime, Document> {

    @Override
    public Document convert(final ZonedDateTime source) {
      if (source == null) {
        return null;
      }

      final Document document = new Document();
      document.put(DATE_TIME, Date.from(source.toInstant()));
      document.put(ZONE, source.getZone().getId());
      return document;
    }

  }
}
