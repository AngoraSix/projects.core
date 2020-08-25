package com.angorasix.projects.core.integration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.bson.Document
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.io.File
import java.text.SimpleDateFormat

/**
 *
 *
 * @author rozagerardo
 */

private fun mapCreatedAt(fieldsMap: MutableMap<String,Any>) {
    val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fieldsMap["dateTime"] as String);
    fieldsMap["dateTime"] = dateTime
}

fun initializeMongodb(jsonFile: String,
                      template: ReactiveMongoTemplate,
                      mapper: ObjectMapper) {
    val file: File = ClassPathResource(jsonFile).file
    val dataEntries: Collection<MutableMap<String, Any>> = mapper.readValue(file.inputStream());

    dataEntries.forEach { entry ->
        //entry["createdAt"] = mapCreatedAt(entry["createdAt"] as MutableMap<String, Any>)
        entry.remove("createdAt");
//        var createdAt = entry.get("createdAt") as MutableMap<String, Object>
//        val asd = LocalDateTime.parse(createdAt["dateTime"] as String)
//        createdAt.put("dateTime",asd as Object)
//        val createdAtDocument = Document(createdAt as Map<String, Any>?)
//        val asd3 = entry as MutableMap
//        asd3.put("createdAt", createdAtDocument as Ob
        //
        //        ject)

//        val document = Document(asd3 as Map<String, Any>?);
//        val asd = mapOf("key" to "value")
        val document = Document();
//        document.
//        ampof con spread y new SimpleDateFormat("dd/MM/yyyy").parse(sDate1)
//        entry.get("createdAt")
        println("GERGER")
        template.insert(document, "project").block()
        println("GERGER2")
    }
}