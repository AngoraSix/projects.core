package com.angorasix.projects.core.infrastructure.persistence;

import com.angorasix.projects.core.infrastructure.persistence.converter.ZonedDateTimeConverters.ZoneDateTimeReaderConverter;
import com.angorasix.projects.core.infrastructure.persistence.converter.ZonedDateTimeConverters.ZoneDateTimeWritingConverter;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class MongoConfig {

  @Bean
  public MongoCustomConversions customConversions(final ZoneDateTimeReaderConverter readerConverter,
      final ZoneDateTimeWritingConverter writerConverter) {
    return new MongoCustomConversions(Arrays.asList(readerConverter, writerConverter));
  }

}
