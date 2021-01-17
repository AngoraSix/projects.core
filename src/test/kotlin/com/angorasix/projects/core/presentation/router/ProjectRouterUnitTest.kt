package com.angorasix.projects.core.presentation.router

import com.angorasix.projects.core.presentation.dto.ProjectDto
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpMethod
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.reactive.function.server.MockServerRequest.builder
import org.springframework.mock.web.server.MockServerWebExchange
import java.net.URI

@ExtendWith(MockKExtension::class)
class ProjectRouterUnitTest {

    private lateinit var router: ProjectRouter

    @BeforeEach
    fun init(@MockK handler: ProjectHandler) {
        router = ProjectRouter(handler)
    }

    @Test
    @Throws(Exception::class)
    fun `Given Project router - When expected APIs requested - Then router routes correctly`() = runBlockingTest {
        val outputRouter = router.projectRouterFunction()
        val mockedRequest = MockServerHttpRequest.get("/mocked")
        val mockedExchange = MockServerWebExchange.builder(mockedRequest)
            .build()
        val getAllProjectsRequest = builder().uri(URI("/projects/"))
            .exchange(mockedExchange)
            .build()
        val getSingleProjectRequest = builder().uri(URI("/projects/1"))
            .exchange(mockedExchange)
            .build()
        val getCreateProjectRequest = builder().method(HttpMethod.POST)
            .uri(URI("/projects/"))
            .exchange(mockedExchange)
            .body(
                ProjectDto(
                    "testProjectId",
                    "testProjectName",
                    emptyList(),
                    emptyList(),
                    null,
                    null
                )
            )
        val invalidRequest = builder().uri(URI("/invalid-path"))
            .exchange(mockedExchange)
            .build()
        // if routes don't match, they will throw an exception as with the invalid Route no need to assert anything
        outputRouter.route(getAllProjectsRequest)
            .awaitSingle()
        outputRouter.route(getSingleProjectRequest)
            .awaitSingle()
        outputRouter.route(getCreateProjectRequest)
            .awaitSingle()
        // disabled until junit-jupiter 5.7.0 is released and included to starter dependency
        assertThrows<NoSuchElementException> {
            outputRouter.route(invalidRequest)
                .awaitSingle()
        }
    }
}
