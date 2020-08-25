package com.angorasix.projects.core.infrastructure.persistence

import com.angorasix.projects.core.infrastructure.persistence.converter.ZonedDateTimeConvertersUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

/**
 * Basic Mongo persistence configuration.
 *
 * @author rozagerardo
 */
@Configuration
class MongoConfig {
    @Bean
    fun customConversions(readerConverter: ZonedDateTimeConvertersUtils.ZonedDateTimeReaderConverter?,
                          writerConverter: ZonedDateTimeConvertersUtils.ZonedDateTimeWritingConverter?): MongoCustomConversions {
        return MongoCustomConversions(listOf(readerConverter, writerConverter))
    }
}