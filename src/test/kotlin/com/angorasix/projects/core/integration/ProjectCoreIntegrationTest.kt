package com.angorasix.projects.core.integration

import com.angorasix.projects.core.ProjectsCoreApplication
import com.angorasix.projects.core.integration.utils.IntegrationProperties
import com.angorasix.projects.core.integration.utils.initializeMongodb
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

@ExtendWith(RestDocumentationExtension::class)
@SpringBootTest(
    classes = [ProjectsCoreApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(locations = ["classpath:integration-application.properties"])
@EnableConfigurationProperties(IntegrationProperties::class)
class ProjectCoreIntegrationTest(
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
        subsectionWithPath("attributes[]").description("Array of the attributes that characterize the project"),
        subsectionWithPath("requirements[]").description("Array of the attributes that are required for the project")
        //            PayloadDocumentation.beneathPath("attributes[]").withSubsectionId("attribute")//.description("Array of the attributes that characterize the project").
    )
    //            fieldWithPath("attributeName").description("Array of the attributes that characterize the project").andWithPrefix("[].", attributeDescriptor),
    //            fieldWithPath("id").description("Project identifier"))

    @BeforeEach
    fun setUp(
        applicationContext: ApplicationContext,
        restDocumentation: RestDocumentationContextProvider
    ) = runBlocking {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
            .configureClient()
            .filter(
                documentationConfiguration(restDocumentation)
            )
            .build()
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
            //                .consumeWith(document("project",
            //                responseFields(fieldWithPath("[]").description("An array of projects")).andWithPrefix("[].",
            //                        *projectDescriptor)))

            .consumeWith(
                document(
                    "project",
                    preprocessResponse(prettyPrint()),
                    //                        responseFields(beneathPath("[].attributes").withSubsectionId("attribute"), *attributeDescriptor),//fieldWithPath("key").description("Attribute id key")),
                    responseFields(fieldWithPath("[]").description("An array of projects")).andWithPrefix(
                        "[].",
                        *projectDescriptor
                    )
                )
            )

            //                .consumeWith(
            //                        document("attribute", responseFields(*attributeDescriptor)))
            .jsonPath("$").isArray.jsonPath("$")
            .value(
                hasSize(2),
                Collection::class.java
            )
        // .jsonPath("$").value(hasSize(2), Collection::class.java).jsonPath("$[].name").value(
        //      Matchers.arrayContainingInAnyOrder(setOf("asd", "ads2")))
    }
}
