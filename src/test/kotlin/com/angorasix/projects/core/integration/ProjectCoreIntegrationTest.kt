package com.angorasix.projects.core.integration

import com.angorasix.projects.core.ProjectsCoreApplication
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.integration.utils.IntegrationProperties
import com.angorasix.projects.core.integration.utils.initializeMongodb
import com.angorasix.projects.core.presentation.dto.AttributeDto
import com.angorasix.projects.core.presentation.dto.ProjectDto
import com.angorasix.projects.core.utils.mockRequestingContributorHeader
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.ZonedDateTime

@SpringBootTest(
    classes = [ProjectsCoreApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestPropertySource(locations = ["classpath:integration-application.properties"])
@EnableConfigurationProperties(IntegrationProperties::class)
class ProjectCoreIntegrationTest(
    @Autowired val mongoTemplate: ReactiveMongoTemplate,
    @Autowired val mapper: ObjectMapper,
    @Autowired val properties: IntegrationProperties,
    @Autowired val webTestClient: WebTestClient,
    @Autowired val apiConfigs: ApiConfigs,
) {

    @BeforeAll
    fun setUp() = runBlocking {
        initializeMongodb(
            properties.mongodb.baseJsonFile,
            mongoTemplate,
            mapper,
        )
    }

    @Test
    fun `Given persisted projects - When request projects list - Then Ok response with projects`() {
        webTestClient.get()
            .uri("/projects-core/")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody() // @formatter:off
            .jsonPath("$").isArray.jsonPath("$.length()")
            .value(
                greaterThanOrEqualTo(2),
            )
            .jsonPath("$..name")
            .value(
                hasItems(
                    "Angora Sustainable",
                    "A Local Project",
                ),
            )
            .jsonPath("$..creatorId")
            .value(
                hasItems(
                    "rozagerardo",
                    "mockUserId",
                ),
            )
            .jsonPath("$..attributes..key")
            .value(
                hasItems(
                    "category",
                    "industry",
                    "location",
                ),
            )
            .jsonPath("$[?(@.name == 'Angora Sustainable')].creatorId")
            .value(contains("rozagerardo"))
            .jsonPath("$[?(@.name == 'Angora Sustainable')].attributes.length()")
            .isEqualTo(5)
            .jsonPath("$[?(@.name == 'Angora Sustainable')].requirements[?(@.key == 'technology')].value")
            .value(contains("Kotlin"))
            // Project 2
            .jsonPath("$[?(@.name == 'A Local Project')].creatorId")
            .value(contains("mockUserId"))
            .jsonPath("$[?(@.name == 'A Local Project')].createdAt")
            .value(
                contains(
                    "2020-09-01T00:00:00-03:00",
                ),
            )
            .jsonPath("$[?(@.name == 'A Local Project')].attributes.length()")
            .isEqualTo(3)
            .jsonPath("$[?(@.name == 'A Local Project')].attributes[?(@.key == 'location')].value")
            .value(contains("Argentina/Cordoba/Cordoba"))
            .jsonPath("$[?(@.name == 'A Local Project')].requirements.length()")
            .isEqualTo(2)
        // @formatter:on
    }

    @Test
    fun `Given persisted projects - When request existing project - Then Ok response with two persisted projects`() {
        val initElementQuery = Query()
        initElementQuery.addCriteria(Criteria.where("name").`is`("Angora Sustainable"))
        val elementId = mongoTemplate.findOne(initElementQuery, Project::class.java).block()?.id

        webTestClient.get()
            .uri("/projects-core/$elementId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody() // @formatter:off
            .jsonPath("$.name")
            .isEqualTo("Angora Sustainable")
            .jsonPath("$.creatorId")
            .isEqualTo("rozagerardo")
            .jsonPath("$.attributes.length()")
            .isEqualTo(5)
            .jsonPath("$.requirements[?(@.key == 'technology')].value")
            .value(contains("Kotlin"))
            .jsonPath("$.createdAt")
            .isEqualTo("2020-08-09T00:23:00-03:00")
            .jsonPath("$.adminId")
            .value(IsNull.nullValue())

        // @formatter:on
    }

    @Test
    fun `Given Project and Angorasix Header - When create new Project - Then Created`() {
        val newProject = ProjectDto(
            "id1",
            "name1",
            mutableSetOf(
                AttributeDto(
                    "attribute1Key",
                    "attribute1Value",
                ),
            ),
            mutableSetOf(
                AttributeDto(
                    "requirement1Key",
                    "requirement1Value",
                ),
            ),
            null,
            null,
            ZonedDateTime.now(),
        )
        webTestClient.post()
            .uri("/projects-core/")
            .accept(MediaType.APPLICATION_JSON)
            .header(apiConfigs.headers.contributor, mockRequestingContributorHeader())
            .body(
                Mono.just(newProject),
                ProjectDto::class.java,
            )
            .exchange()
            .expectStatus().isCreated.expectBody() // @formatter:off
            .jsonPath("$.name").isEqualTo("name1")
            .jsonPath("$.creatorId").isEqualTo("mockedContributorId1")
            .jsonPath("$.requirements.length()").isEqualTo(1)
            .jsonPath("$.attributes[0].key").isEqualTo("attribute1Key")
            .jsonPath("$.attributes[0].value").isEqualTo("attribute1Value")
            .jsonPath("$.id").value(allOf(not("id1"), notNullValue()))
            .jsonPath("$.requirements.length()").isEqualTo(1)
            .jsonPath("$.requirements[0].key").isEqualTo("requirement1Key")
            .jsonPath("$.requirements[0].value").isEqualTo("requirement1Value")
            .jsonPath("$.createdAt").exists()
            .jsonPath("$.adminId").isEqualTo("mockedContributorId1")
        // @formatter:on
    }
}
