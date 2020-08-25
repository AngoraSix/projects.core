package com.angorasix.projects.core.infrastructure.persistence.converter

import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * Converter to convert between ZonedDateTime and Mongodb's Document.
 *
 * @author rozagerardo
 */
object ZonedDateTimeConvertersUtils {
    const val DATE_TIME = "dateTime"
    const val ZONE = "zone"

    @Component
    @ReadingConverter
    class ZonedDateTimeReaderConverter : Converter<Document?, ZonedDateTime?> {
        override fun convert(source: Document): ZonedDateTime? {
            source?.let {
                val dateTime = it.getDate(DATE_TIME)
                val zoneId = it.getString(ZONE)
                val zone = ZoneId.of(zoneId)
                return ZonedDateTime.ofInstant(dateTime.toInstant(), zone)
            }
        }
    }

    @Component
    @WritingConverter
    class ZonedDateTimeWritingConverter : Converter<ZonedDateTime?, Document?> {
        override fun convert(source: ZonedDateTime): Document? {
            source?.let {
                val document = Document()
                document[DATE_TIME] = Date.from(it.toInstant())
                document[ZONE] = it.zone.id
                return document
            }
        }
    }
}