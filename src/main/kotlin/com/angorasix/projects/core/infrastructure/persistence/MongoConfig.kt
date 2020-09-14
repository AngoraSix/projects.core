package com.angorasix.projects.core.infrastructure.persistence
//
//import com.angorasix.projects.core.infrastructure.persistence.converter.ZonedDateTimeConvertersUtils
//import org.springframework.data.mongodb.core.convert.MongoCustomConversions
//
///**
// * Basic Mongo persistence configuration.
// *
// * @author rozagerardo
// */
//class MongoConfig {
//
//    fun customConversions(readerConverter: ZonedDateTimeConvertersUtils.ZonedDateTimeReaderConverter?,
//                          writerConverter: ZonedDateTimeConvertersUtils.ZonedDateTimeWritingConverter?): MongoCustomConversions {
//        return MongoCustomConversions(listOf(readerConverter, writerConverter))
//    }
//}