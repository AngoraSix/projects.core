package com.angorasix.projects.core.infrastructure.persistence

import com.angorasix.projects.core.infrastructure.persistence.converter.ZonedDateTimeConvertersUtils
import org.assertj.core.api.Assertions
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import java.time.LocalDateTime
import java.time.ZonedDateTime

class MongoConfigUnitTest {

    @Test
    fun whenCustomConversionsInvoked_thenMongoConversionsWithZonedDateTimeConvertersRetrieved() {
        val readerConverter = ZonedDateTimeConvertersUtils.ZonedDateTimeReaderConverter()
        val writerConverter = ZonedDateTimeConvertersUtils.ZonedDateTimeWritingConverter()
        val conversionsOutput = MongoCustomConversions(
            listOf(
                ZonedDateTimeConvertersUtils.ZonedDateTimeReaderConverter(),
                ZonedDateTimeConvertersUtils.ZonedDateTimeWritingConverter()
            )
        )

        // handled conversions
        Assertions.assertThat(conversionsOutput.getCustomWriteTarget(ZonedDateTime::class.java).get().name)
            .isEqualTo("org.bson.Document")
        Assertions.assertThat(
            conversionsOutput.hasCustomReadTarget(
                Document::class.java, ZonedDateTime::class.java
            )
        ).isTrue()
        // not handled conversions
        Assertions.assertThat(conversionsOutput.getCustomWriteTarget(Document::class.java)).isEmpty
        Assertions.assertThat(
            conversionsOutput.hasCustomReadTarget(
                Document::class.java, LocalDateTime::class.java
            )
        ).isFalse()
    }
}