package com.angorasix.projects.core.integration.docs

import com.angorasix.projects.core.ProjectsCoreApplication
import com.angorasix.projects.core.domain.project.Attribute
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.integration.utils.IntegrationProperties
import com.angorasix.projects.core.integration.utils.initializeMongodb
import com.angorasix.projects.core.presentation.dto.ProjectDto
import com.angorasix.projects.core.utils.mockRequestingContributorHeader
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.hateoas.MediaTypes
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.links
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.beneathPath
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.body
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Mono
import java.time.Duration

@ExtendWith(RestDocumentationExtension::class)
@SpringBootTest(
    classes = [ProjectsCoreApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestPropertySource(locations = ["classpath:integration-application.properties"])
@EnableConfigurationProperties(IntegrationProperties::class)
class ProjectCoreDocsIntegrationTest(
    @Autowired val mongoTemplate: ReactiveMongoTemplate,
    @Autowired val mapper: ObjectMapper,
    @Autowired val properties: IntegrationProperties,
    @Autowired val apiConfigs: ApiConfigs,
) {

    private lateinit var webTestClient: WebTestClient

    var attributeDescriptor = arrayOf<FieldDescriptor>(
        fieldWithPath("key").description("Key identifier for the attribute"),
        fieldWithPath("value").description("The value for the particular attribute"),
    )

    var projectDescriptor = arrayOf<FieldDescriptor>(
        fieldWithPath("name").description("Name of the project"),
        fieldWithPath("id").description("Project identifier"),
        fieldWithPath("creatorId").description("Contributor identifier"),
        fieldWithPath("adminId").optional().description("Identifier of the Admin Contributor"),
        fieldWithPath("createdAt").description("Date in which the project was created"),
        subsectionWithPath("attributes[]").type(ArrayOfFieldType(Attribute::class.simpleName))
            .description("Array of the attributes that characterize the project"),
        subsectionWithPath("requirements[]").type(ArrayOfFieldType(Attribute::class.simpleName))
            .description("Array of the attributes that are required for the project"),

        // until we resolve and unify the list and single response links, all will be marked as optional
        subsectionWithPath("links").optional().description("HATEOAS links")
            .type(JsonFieldType.ARRAY),
        subsectionWithPath("_links").optional().description("HATEOAS links")
            .type(JsonFieldType.OBJECT),
        subsectionWithPath("_templates").optional()
            .description("HATEOAS HAL-FORM links template info").type(
                JsonFieldType.OBJECT,
            ),
    )

    var projectPostBodyDescriptor = arrayOf<FieldDescriptor>(
        fieldWithPath("name").description("Name of the project"),
        fieldWithPath("id").ignored(),
        fieldWithPath("creatorId").ignored(),
        fieldWithPath("adminId").ignored(),
        fieldWithPath("createdAt").ignored(),
        subsectionWithPath("attributes[]").ignored(),
        subsectionWithPath("requirements[]").ignored(),
        fieldWithPath("links[]").ignored(),
    )

    @BeforeAll
    fun setUpDb() = runBlocking {
        initializeMongodb(properties.mongodb.baseJsonFile, mongoTemplate, mapper)
    }

    @BeforeEach
    fun setUpWebClient(
        applicationContext: ApplicationContext,
        restDocumentation: RestDocumentationContextProvider,
    ) = runBlocking {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).configureClient()
            .responseTimeout(Duration.ofMillis(30000))
            .filter(documentationConfiguration(restDocumentation)).filter(
                ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
                    println("Request: ${clientRequest.method()} ${clientRequest.url()}")
                    clientRequest.headers()
                        .forEach { name, values -> values.forEach { value -> println("$name=$value") } }
                    Mono.just(clientRequest)
                },
            ).build()
    }

    @Test
    fun `Given persisted projects - When execute and document requests - Then everything documented`() {
        executeAndDocumentGetListProjectsRequest()
        executeAndDocumentGetSingleProjectRequest()
        executeAndDocumentPostCreateProjectRequest()
    }

    private fun executeAndDocumentPostCreateProjectRequest() {
        val newProject = ProjectDto("New Project Name")
        webTestClient.post()
            .uri("/projects-core/")
            .accept(MediaTypes.HAL_FORMS_JSON)
            .contentType(MediaTypes.HAL_FORMS_JSON)
            .header(apiConfigs.headers.contributor, mockRequestingContributorHeader())
            .body(Mono.just(newProject))
            .exchange()
            .expectStatus().isCreated.expectBody().consumeWith(
                document(
                    "project-create",
                    preprocessResponse(prettyPrint()),
                    requestFields(*projectPostBodyDescriptor),
                    responseFields(*projectDescriptor),
                    responseHeaders(
                        headerWithName(HttpHeaders.LOCATION).description("URL of the newly created project"),
                    ),
                    links(
                        halLinks(),
                        linkWithRel("self").description("The self link"),
                        linkWithRel("updateProject").description("Link to edit operation"),
                    ),
                ),
            )
    }

    private fun executeAndDocumentGetSingleProjectRequest() {
        val initElementQuery = Query()
        initElementQuery.addCriteria(Criteria.where("name").`is`("Angora Sustainable"))
        val elementId = mongoTemplate.findOne(initElementQuery, Project::class.java).block()?.id

        webTestClient.get().uri("/projects-core/{projectId}", elementId)
            .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk.expectBody()
            .consumeWith(
                document(
                    "project-single",
                    preprocessResponse(prettyPrint()),
                    pathParameters(parameterWithName("projectId").description("The Project id")),
                    responseFields(*projectDescriptor),
                    links(
                        halLinks(),
                        linkWithRel("self").description("The self link"),
                    ),
                ),
            )
    }

    private fun executeAndDocumentGetListProjectsRequest() {
        webTestClient.get().uri("/projects-core/").accept(MediaType.APPLICATION_JSON).exchange()
            .expectStatus().isOk.expectBody().consumeWith(
                document(
                    "project-list",
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("[]").type(ArrayOfFieldType(Project::class.simpleName))
                            .description("An array of projects"),
                    ).andWithPrefix("[].", *projectDescriptor),
                    responseFields(beneathPath("[].attributes[]").withSubsectionId("attribute")).andWithPrefix(
                        "[].",
                        *attributeDescriptor,
                    ),
                ),
            )
    }

    private class ArrayOfFieldType(private val field: String?) {
        override fun toString(): String = "Array of $field"
    }
}
