package com.angorasix.projects.core.integration

import com.angorasix.projects.core.ProjectsCoreApplication
import com.angorasix.projects.core.integration.utils.IntegrationProperties
import com.angorasix.projects.core.integration.utils.initializeMongodb
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(RestDocumentationExtension::class)
@SpringBootTest(classes = [ProjectsCoreApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = ["classpath:integration-application.properties"])
@EnableConfigurationProperties(IntegrationProperties::class)
class ProjectCoreIntegrationTest(@Autowired val mongoTemplate: ReactiveMongoTemplate,
                                 @Autowired val mapper: ObjectMapper,
                                 @Autowired val properties: IntegrationProperties) {

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp(applicationContext: ApplicationContext,
              restDocumentation: RestDocumentationContextProvider) {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).configureClient().filter(
                documentationConfiguration(restDocumentation)).build()
        initializeMongodb(properties.mongodb.baseJsonFile, mongoTemplate, mapper)
    }

    @ExperimentalStdlibApi
    @Test
    fun `Given persisted projects - When request existing project - Then Ok response`() {
        val asd = mongoTemplate.getCollection("project").block()
        //        asd?.countDocuments()?.collect {
        //            println(it)
        //        }
        //        var asd =
        webTestClient.get().uri("/projects").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk.expectBody().consumeWith {
            println(it.responseBody?.size)
            println(it.responseBody.toString())
            println(it.responseBody?.decodeToString())
        }

        //println(asd.responseBody)
        //println(asd.toString())
        //println (asd.)
        //                .consumeWith(
        //                document("index")).consumeWith {
        //            assertThat(it.responseBody).isNotNull()
        //        }
    }
}