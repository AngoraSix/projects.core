package com.angorasix.projects.core.infrastructure.persistence.converter

import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class ZonedDateTimeConvertersUnitTest {
    private val writingConverter = ZonedDateTimeConvertersUtils.ZonedDateTimeWritingConverter()
    private val readerConverter = ZonedDateTimeConvertersUtils.ZonedDateTimeReaderConverter()

    // Just for coverage purposes
    val converters = ZonedDateTimeConvertersUtils

    @Test
    @Throws(Exception::class)
    fun givenZonedDateTime_whenWritingToDocument_thenCustomFieldsCorrectlyCreated() {
        val inputZdt = ZonedDateTime.of(
            1991,
            4,
            22,
            23,
            0,
            0,
            0,
            ZoneId.of("America/Argentina/Cordoba"),
        )
        val outputDocument = writingConverter.convert(inputZdt)
        assertThat<Any>(outputDocument!!["zone"]).isEqualTo("America/Argentina/Cordoba")
        Assertions.assertThat(outputDocument.getDate("dateTime"))
            .isEqualTo(
                Date.from(
                    inputZdt.withZoneSameInstant(
                        ZoneId.of("UTC"),
                    )
                        .toInstant(),
                ),
            )
    }

    @Test
    @Throws(Exception::class)
    fun givenDifferentZonedDateTime_whenWritingToDocument_thenCustomFieldsCorrectlyCreated() {
        val inputZdt = ZonedDateTime.of(
            1991,
            4,
            22,
            15,
            0,
            0,
            0,
            ZoneId.of("Pacific/Auckland"),
        )
        val outputDocument = writingConverter.convert(inputZdt)
        assertThat<Any>(outputDocument!!["zone"]).isEqualTo("Pacific/Auckland")
        Assertions.assertThat(outputDocument.getDate("dateTime"))
            .isEqualTo(
                Date.from(
                    inputZdt.withZoneSameInstant(
                        ZoneId.of("UTC"),
                    )
                        .toInstant(),
                ),
            )
    }

    @Test
    @Throws(Exception::class)
    fun givenDocumentWithCorrectFields_whenReadingDocument_thenZonedDateTimeCorrectlyCreated() {
        val baseZdt = ZonedDateTime.of(
            1991,
            4,
            22,
            23,
            0,
            0,
            0,
            ZoneId.of("America/Argentina/Cordoba"),
        )
        val inputDocumentMap: MutableMap<String, Any> = HashMap()
        inputDocumentMap["zone"] = "America/Argentina/Cordoba"
        inputDocumentMap["dateTime"] = Date.from(
            baseZdt.withZoneSameInstant(ZoneId.of("UTC"))
                .toInstant(),
        )
        val inputDocument = Document(inputDocumentMap)
        val outputTdz = readerConverter.convert(inputDocument)
        Assertions.assertThat(outputTdz)
            .isEqualTo(baseZdt)
        Assertions.assertThat(outputTdz.toString())
            .isEqualTo("1991-04-22T23:00-04:00[America/Argentina/Cordoba]")
    }
}
