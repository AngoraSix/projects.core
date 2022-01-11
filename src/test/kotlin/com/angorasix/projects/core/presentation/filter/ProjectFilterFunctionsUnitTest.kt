package com.angorasix.projects.core.presentation.filter

import com.angorasix.projects.core.infrastructure.config.ApiConfigs
import com.angorasix.projects.core.infrastructure.config.ServiceConfigs
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes
import org.assertj.core.api.Condition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait

/**
 * @author rozagerardo
 */
@ExtendWith(MockKExtension::class)
public class ProjectFilterFunctionsUnitTest {

    @MockK
    private lateinit var objectMapper: ObjectMapper

    private lateinit var serviceConfigs: ServiceConfigs

    @BeforeEach
    fun init() {
        serviceConfigs = ServiceConfigs()
        serviceConfigs.api = ApiConfigs("MockedContributorHeader")
    }

    @Test
    @Throws(Exception::class)
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun `Given request - When headerFilterFunction invoked - Then response contains attribute`() = runBlockingTest {
        val mockedRequest: ServerRequest = MockServerRequest.builder()
                .header(
                        "Angorasix-API",
                        "value1"
                )
                .build()
        val next: suspend (request: ServerRequest) -> ServerResponse = {
            ServerResponse.ok()
                    .buildAndAwait()
        }

        val outputResponse = headerFilterFunction(
                mockedRequest,
                next,
                serviceConfigs,
                objectMapper
        )

        AssertionsForClassTypes.assertThat(outputResponse.statusCode())
                .isEqualTo(HttpStatus.OK)
        assertThat(mockedRequest.attributes()).hasEntrySatisfying(
                "ger1",
                Condition(
                        { (it as Collection<*>).contains("value1") },
                        "Request attribute contains ger1 entry"
                )
        )
    }
}
