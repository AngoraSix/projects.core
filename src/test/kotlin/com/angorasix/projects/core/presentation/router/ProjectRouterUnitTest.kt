package com.angorasix.projects.core.presentation.router

import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.BasePathConfigs
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.HeadersConfigs
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.Route
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.RoutesConfigs
import com.angorasix.projects.core.presentation.dto.ProjectDto
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpMethod
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.reactive.function.server.MockServerRequest.builder
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.function.server.EntityResponse
import java.net.URI

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ProjectRouterUnitTest {

    private lateinit var router: ProjectRouter

    private var headerConfigs: HeadersConfigs = HeadersConfigs("MockedContributorHeader")
    private var routeConfigs: RoutesConfigs = RoutesConfigs(
        "",
        "/{id}",
        Route("mocked-create", listOf("mocked-base1"), HttpMethod.POST, ""),
        Route("mocked-update", listOf("mocked-base1"), HttpMethod.PUT, "/{id}"),
        Route(
            "mocked-validate-admin",
            listOf("mocked-base1"),
            HttpMethod.GET,
            "/mocked-validate-admin",
        ),
        Route("mocked-get-single", listOf("mocked-base1"), HttpMethod.GET, "/{id}"),
        Route("mocked-list-project", listOf("mocked-base1"), HttpMethod.GET, ""),
    )
    private var basePathsConfigs: BasePathConfigs = BasePathConfigs("/mocked-projects-core")

    @MockK
    private lateinit var handler: ProjectHandler

    @BeforeEach
    fun init(@MockK apiConfigs: ApiConfigs, @MockK objectMapper: ObjectMapper) {
        every { apiConfigs.headers } returns headerConfigs
        every { apiConfigs.routes } returns routeConfigs
        every { apiConfigs.basePaths } returns basePathsConfigs
        router = ProjectRouter(handler, objectMapper, apiConfigs)
    }

    @Test
    @Throws(Exception::class)
    fun `Given Project router - When expected APIs requested - Then router routes correctly`() =
        runBlockingTest {
            val outputRouter = router.projectRouterFunction()
            val mockedRequest =
                MockServerHttpRequest.get("mocked-any") // basePathsConfigs.projectsCore + routeConfigs.baseListCrudRoute + routeConfigs.listProjects.path)
            val mockedExchange = MockServerWebExchange.builder(mockedRequest).build()
            val getAllProjectsRequest =
                builder().uri(URI(basePathsConfigs.projectsCore + routeConfigs.listProjects.path))
                    .exchange(mockedExchange).build()
            val getSingleProjectRequest =
                builder().uri(URI(basePathsConfigs.projectsCore + "/1")).exchange(mockedExchange)
                    .build()
            val createProjectRequest = builder().method(HttpMethod.POST)
                .uri(URI(basePathsConfigs.projectsCore + routeConfigs.createProject.path))
                .exchange(mockedExchange).body(
                    ProjectDto(
                        "testProjectId",
                        "testProjectName",
                    ),
                )
            val updateProjectRequest =
                builder().method(HttpMethod.PUT).uri(URI(basePathsConfigs.projectsCore + "/1"))
                    .exchange(mockedExchange).body(
                        ProjectDto(
                            "testProjectNameUpdated",
                        ),
                    )
            val validateAdminRequest = builder().method(HttpMethod.GET)
                .uri(URI(basePathsConfigs.projectsCore + routeConfigs.validateAdminUser.path))
                .exchange(mockedExchange).build()
            val invalidRequest =
                builder().uri(URI("/invalid-path")).exchange(mockedExchange).build()

            val mockedResponse = EntityResponse.fromObject("any").build().awaitSingle()
            coEvery { handler.listProjects(getAllProjectsRequest) } returns mockedResponse
            coEvery { handler.getProject(getSingleProjectRequest) } returns mockedResponse
            coEvery { handler.createProject(getSingleProjectRequest) } returns mockedResponse
            coEvery { handler.updateProject(getSingleProjectRequest) } returns mockedResponse
            coEvery { handler.validateAdminUser(getSingleProjectRequest) } returns mockedResponse
            // if routes don't match, they will throw an exception as with the invalid Route no need to assert anything
            outputRouter.route(getAllProjectsRequest).awaitSingle().handle(getAllProjectsRequest)
                .awaitSingle()
            outputRouter.route(getSingleProjectRequest).awaitSingle()
                .handle(getSingleProjectRequest).awaitSingle()
            outputRouter.route(createProjectRequest).awaitSingle().handle(createProjectRequest)
                .awaitSingle()
            outputRouter.route(updateProjectRequest).awaitSingle().handle(updateProjectRequest)
                .awaitSingle()
            outputRouter.route(validateAdminRequest).awaitSingle().handle(validateAdminRequest)
                .awaitSingle()
            // disabled until junit-jupiter 5.7.0 is released and included to starter dependency
            assertThrows<NoSuchElementException> {
                outputRouter.route(invalidRequest).awaitSingle()
            }
        }
}
