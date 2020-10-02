package com.angorasix.projects.core.integration

import com.angorasix.projects.core.ProjectsCoreApplication
import com.angorasix.projects.core.integration.utils.IntegrationProperties
import com.angorasix.projects.core.integration.utils.initializeMongodb
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    classes = [ProjectsCoreApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(locations = ["classpath:integration-application.properties"])
@EnableConfigurationProperties(IntegrationProperties::class)
class ProjectCoreIntegrationTest(
    @Autowired val mongoTemplate: ReactiveMongoTemplate,
    @Autowired val mapper: ObjectMapper,
    @Autowired val properties: IntegrationProperties,
    @Autowired val webTestClient: WebTestClient
) {

    @BeforeEach
    fun setUp() = runBlocking {
        initializeMongodb(
            properties.mongodb.baseJsonFile,
            mongoTemplate,
            mapper
        )
    }

    @Test
    fun `Given persisted projects - When request existing project - Then Ok response`() {
        webTestClient.get()
            .uri("/projects")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            // @formatter:off
            .jsonPath("$").isArray.jsonPath("$")
                .value(
                    hasSize(2),
                    Collection::class.java
                )
            .jsonPath("$..name")
                .value(
                    hasItems(
                        "Angora Sustainable",
                        "A Local Project"
                    )
                )
            .jsonPath("$..creatorId")
                .value(
                    hasItems(
                        "rozagerardo",
                        "mockUserId"
                    )
                )
            .jsonPath("$..attributes..key")
                .value(
                    hasItems(
                        "category",
                        "industry",
                        "location"
                    )
                )
            .jsonPath("$[?(@.id == '1')].creatorId")
                .value(contains("rozagerardo"))
            .jsonPath("$[?(@.id == '1')].name")
                .value(contains("Angora Sustainable"))
            .jsonPath("$[?(@.id == '1')].attributes.length()")
                .isEqualTo(5)
            .jsonPath("$[?(@.id == '1')].requirements[?(@.key == 'technology')].value")
                .value(contains("Kotlin"))
            // Project 2
            .jsonPath("$[?(@.id == '2')].name")
                .value(contains("A Local Project"))
            .jsonPath("$[?(@.id == '2')].creatorId")
                .value(contains("mockUserId"))
            .jsonPath("$[?(@.id == '2')].createdAt")
                .value(
                    contains(
                        "2020-09-01T00:00:00-03:00"
                    )
                )
            .jsonPath("$[?(@.id == '2')].attributes.length()")
                .isEqualTo(3)
            .jsonPath("$[?(@.id == '2')].attributes[?(@.key == 'location')].value")
                .value(contains("Argentina/Cordoba/Cordoba"))
            .jsonPath("$[?(@.id == '2')].requirements.length()")
                .isEqualTo(2)
            // @formatter:on
    }
}
