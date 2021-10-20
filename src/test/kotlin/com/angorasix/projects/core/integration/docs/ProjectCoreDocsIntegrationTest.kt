package com.angorasix.projects.core.integration.docs

import com.angorasix.projects.core.ProjectsCoreApplication
import com.angorasix.projects.core.domain.project.Attribute
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.integration.utils.IntegrationProperties
import com.angorasix.projects.core.integration.utils.initializeMongodb
import com.angorasix.projects.core.presentation.dto.ProjectDto
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
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.FieldDescriptor
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
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(locations = ["classpath:integration-application.properties"])
@EnableConfigurationProperties(IntegrationProperties::class)
class ProjectCoreDocsIntegrationTest(
    @Autowired val mongoTemplate: ReactiveMongoTemplate,
    @Autowired val mapper: ObjectMapper,
    @Autowired val properties: IntegrationProperties
) {

    private lateinit var webTestClient: WebTestClient

    var attributeDescriptor = arrayOf<FieldDescriptor>(
        fieldWithPath("key").description("Key identifier for the attribute"),
        fieldWithPath("value").description("The value for the particular attribute")
    )

    var projectDescriptor = arrayOf<FieldDescriptor>(
        fieldWithPath("name").description("Name of the project"),
        fieldWithPath("id").description("Project identifier"),
        fieldWithPath("creatorId").description("Contributor identifier"),
        fieldWithPath("createdAt").description("Date in which the project was created"),
        subsectionWithPath("attributes[]").type(ArrayOfFieldType(Attribute::class.simpleName))
            .description("Array of the attributes that characterize the project"),
        subsectionWithPath("requirements[]").type(ArrayOfFieldType(Attribute::class.simpleName))
            .description("Array of the attributes that are required for the project")
    )

    var projectPostBodyDescriptor = arrayOf<FieldDescriptor>(
        fieldWithPath("name").description("Name of the project"),
        fieldWithPath("id").ignored(),
        fieldWithPath("creatorId").ignored(),
        fieldWithPath("createdAt").ignored(),
        subsectionWithPath("attributes[]").ignored(),
        subsectionWithPath("requirements[]").ignored()
    )

    @BeforeAll
    fun setUpDb() = runBlocking {
        initializeMongodb(
            properties.mongodb.baseJsonFile,
            mongoTemplate,
            mapper
        )
    }

    @BeforeEach
    fun setUpWebClient(
        applicationContext: ApplicationContext,
        restDocumentation: RestDocumentationContextProvider
    ) = runBlocking {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
            .configureClient()
            .responseTimeout(Duration.ofMillis(30000))
            .filter(
                documentationConfiguration(restDocumentation)
            )
            .filter(ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
                println(
                    "Request: ${clientRequest.method()} ${clientRequest.url()}"
                )
                clientRequest.headers()
                    .forEach { name, values ->
                        values.forEach { value ->
                            println(
                                "$name=$value",
                            )
                        }
                    }
                Mono.just(clientRequest)
            })
            .build()
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
            .uri(
                "/projects/",
            )
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(newProject))
            .exchange()
            .expectStatus().isCreated.expectBody()
            .consumeWith(
                document(
                    "project-create",
                    preprocessResponse(prettyPrint()),
                    requestFields(*projectPostBodyDescriptor),
                    responseFields(*projectDescriptor),
                    responseHeaders(
                        headerWithName(HttpHeaders.LOCATION).description("URL of the newly created project")
                    )
                )
            )
    }

    private fun executeAndDocumentGetSingleProjectRequest() {
        webTestClient.get()
            .uri(
                "/projects/{projectId}",
                1
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            .consumeWith(
                document(
                    "project-single",
                    preprocessResponse(prettyPrint()),
                    pathParameters(parameterWithName("projectId").description("The Project id")),
                    responseFields(*projectDescriptor)
                )
            )
    }

    private fun executeAndDocumentGetListProjectsRequest() {
        webTestClient.get()
            .uri("/projects/")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            .consumeWith(
                document(
                    "project-list",
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("[]").type(ArrayOfFieldType(Project::class.simpleName))
                            .description("An array of projects")
                    ).andWithPrefix(
                        "[].",
                        *projectDescriptor
                    ),
                    responseFields(
                        beneathPath("[].attributes[]").withSubsectionId("attribute")
                    ).andWithPrefix(
                        "[].",
                        *attributeDescriptor
                    )
                )
            )
    }

    private class ArrayOfFieldType(private val field: String?) {
        override fun toString(): String = "Array of $field"
    }
}
