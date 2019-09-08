package com.angorasix.projects.core.infrastructure.persistence.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.angorasix.projects.core.infrastructure.persistence.converter.ZonedDateTimeConverters.ZoneDateTimeReaderConverter;
import com.angorasix.projects.core.infrastructure.persistence.converter.ZonedDateTimeConverters.ZoneDateTimeWritingConverter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.Test;

public class ZonedDateTimeConvertersUnitTest {

  final ZoneDateTimeWritingConverter writingConverter = new ZoneDateTimeWritingConverter();
  final ZoneDateTimeReaderConverter readerConverter = new ZoneDateTimeReaderConverter();

  @Test
  public void givenZonedDateTime_whenWritingToDocument_thenCustomFieldsCorrectlyCreated()
      throws Exception {
    final ZonedDateTime inputZdt =
        ZonedDateTime.of(1991, 04, 22, 23, 00, 00, 0, ZoneId.of("America/Argentina/Cordoba"));

    final Document outputDocument = writingConverter.convert(inputZdt);

    assertThat(outputDocument.get("zone")).isEqualTo("America/Argentina/Cordoba");
    assertThat(outputDocument.getDate("dateTime"))
        .isEqualTo(Date.from(inputZdt.withZoneSameInstant(ZoneId.of("UTC")).toInstant()));
  }

  @Test
  public void asdgivenZonedDateTime_whenWritingToDocument_thenCustomFieldsCorrectlyCreated()
      throws Exception {
    final ZonedDateTime inputZdt =
        ZonedDateTime.of(1991, 04, 22, 15, 00, 00, 0, ZoneId.of("Pacific/Auckland"));

    final Document outputDocument = writingConverter.convert(inputZdt);

    assertThat(outputDocument.get("zone")).isEqualTo("Pacific/Auckland");
    assertThat(outputDocument.getDate("dateTime"))
        .isEqualTo(Date.from(inputZdt.withZoneSameInstant(ZoneId.of("UTC")).toInstant()));
  }

  @Test
  public void givenDocumentWithCorrectFields_whenReadingDocument_thenZonedDateTimeCorrectlyCreated()
      throws Exception {
    final ZonedDateTime baseZdt =
        ZonedDateTime.of(1991, 04, 22, 23, 00, 00, 0, ZoneId.of("America/Argentina/Cordoba"));
    final Map<String, Object> inputDocumentMap = new HashMap<>();
    inputDocumentMap.put("zone", "America/Argentina/Cordoba");
    inputDocumentMap.put("dateTime",
        Date.from(baseZdt.withZoneSameInstant(ZoneId.of("UTC")).toInstant()));
    final Document inputDocument = new Document(inputDocumentMap);

    ZonedDateTime outputTdz = readerConverter.convert(inputDocument);

    assertThat(outputTdz).isEqualTo(baseZdt);
    assertThat(outputTdz.toString()).isEqualTo("1991-04-22T23:00-04:00[America/Argentina/Cordoba]");
  }
}
