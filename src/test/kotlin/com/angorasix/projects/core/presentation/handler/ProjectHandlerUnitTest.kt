package com.angorasix.projects.core.presentation.handler

import com.angorasix.commons.domain.RequestingContributor
import com.angorasix.commons.infrastructure.presentation.error.ErrorResponseBody
import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.HeadersConfigs
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.Route
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.RoutesConfigs
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import com.angorasix.projects.core.presentation.dto.IsAdminDto
import com.angorasix.projects.core.presentation.dto.ProjectDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.function.server.EntityResponse
import org.springframework.web.reactive.function.server.ServerRequest
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ProjectHandlerUnitTest {

    private lateinit var handler: ProjectHandler

    @MockK
    private lateinit var service: ProjectService

    @MockK
    private lateinit var apiConfigs: ApiConfigs

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

    @BeforeEach
    fun init() {
        every { apiConfigs.headers } returns headerConfigs
        every { apiConfigs.routes } returns routeConfigs
        handler = ProjectHandler(service, apiConfigs)
    }

    @Test
    @Throws(Exception::class)
    fun `Given existing projects - When list projects - Then handler retrieves Ok Response`() =
        runBlockingTest {
            val mockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(routeConfigs.listProjects.path).build(),
            )
            val mockedRequest: ServerRequest =
                MockServerRequest.builder().exchange(mockedExchange).build()
            val mockedProject =
                Project("mockedProjectName", "creator_id", "creator_id", ZoneId.systemDefault())
            val retrievedProject = flowOf(mockedProject)
            coEvery { service.findProjects(ListProjectsFilter()) } returns retrievedProject

            val outputResponse = handler.listProjects(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<Flow<ProjectDto>>
            val responseBody = response.entity()
            responseBody.collect {
                assertThat(it.name).isEqualTo("mockedProjectName")
                assertThat(it.creatorId).isEqualTo("creator_id")
            }
            coVerify { service.findProjects(ListProjectsFilter()) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with project and RequestingContributor - When create project - Then handler retrieves Created`() =
        runBlocking { // = runBlockingTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectDto = ProjectDto(
                null,
                "mockedInputProjectName",
            )
            val mockedRequestingContributor = RequestingContributor("mockedId")
            val mockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(routeConfigs.createProject.path).build(),
            )
            val mockedRequest: ServerRequest = MockServerRequest.builder()
                .attribute(headerConfigs.contributor, mockedRequestingContributor)
                .exchange(mockedExchange).body(mono { mockedProjectDto })
            val mockedProject = Project(
                "mockedProjectName",
                "creator_id",
                "creator_id",
                ZoneId.systemDefault(),
            )
            coEvery { service.createProject(ofType(Project::class)) } returns mockedProject

            val outputResponse = handler.createProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.CREATED)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<ProjectDto>
            val responseBody = response.entity()
            assertThat(responseBody).isNotSameAs(mockedProjectDto)
            assertThat(responseBody.name).isEqualTo("mockedProjectName")
            assertThat(responseBody.creatorId).isEqualTo("creator_id")
            coVerify { service.createProject(ofType(Project::class)) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with project and no RequestingContributor - When create project - Then handler retrieves Bad Request`() =
        runBlocking { // = runBlockingTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectDto = ProjectDto(
                null,
                "mockedInputProjectName",
            )
            val mockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(routeConfigs.createProject.path).build(),
            )
            val mockedRequest: ServerRequest =
                MockServerRequest.builder().exchange(mockedExchange).body(mono { mockedProjectDto })
            val mockedProject = Project(
                "mockedProjectName",
                "creator_id",
                "creator_id",
                ZoneId.systemDefault(),
            )
            coEvery { service.createProject(ofType(Project::class)) } returns mockedProject

            val outputResponse = handler.createProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<ErrorResponseBody>
            val responseBody = response.entity()
            assertThat(responseBody.status).isEqualTo(400)
            assertThat(responseBody.errorCode).isEqualTo("CONTRIBUTOR_HEADER_INVALID")
            Unit
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with project and RequestingContributor - When update project - Then handler retrieves Updated`() =
        runBlocking { // = runBlockingTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectDto = ProjectDto(
                null,
                "mockedInputProjectName",
            )
            val mockedRequestingContributor = RequestingContributor("mockedId")
            val mockedExchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/id1-mocked").build())
            val mockedRequest: ServerRequest = MockServerRequest.builder()
                .attribute(headerConfigs.contributor, mockedRequestingContributor)
                .pathVariable("id", "id1").exchange(mockedExchange).body(mono { mockedProjectDto })
            val mockedProject = Project(
                "mockedProjectName",
                "creator_id",
                "creator_id",
                ZoneId.systemDefault(),
            )
            coEvery { service.updateProject("id1", ofType(Project::class)) } returns mockedProject

            val outputResponse = handler.updateProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<ProjectDto>
            val responseBody = response.entity()
            assertThat(responseBody).isNotSameAs(mockedProjectDto)
            assertThat(responseBody.name).isEqualTo("mockedProjectName")
            assertThat(responseBody.creatorId).isEqualTo("creator_id")
            coVerify { service.updateProject("id1", ofType(Project::class)) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given existing projects - When get project for non Admin contributor - Then handler retrieves Ok Response without Edit link`() =
        runBlockingTest {
            val projectId = "projectId"
            val mockedRequestingContributor = RequestingContributor("mockedId")
            val mockedExchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/id1-mocked").build())
            val mockedRequest: ServerRequest =
                MockServerRequest.builder()
                    .attribute(headerConfigs.contributor, mockedRequestingContributor)
                    .pathVariable("id", projectId).exchange(mockedExchange).build()
            val mockedProject =
                Project("mockedProjectName", "creator_id", "otherId", ZoneId.systemDefault())
            coEvery { service.findSingleProject(projectId) } returns mockedProject

            val outputResponse = handler.getProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val responseBody =
                @Suppress("UNCHECKED_CAST")
                (outputResponse as EntityResponse<ProjectDto>).entity()
            assertThat(responseBody.name).isEqualTo("mockedProjectName")
            assertThat(responseBody.creatorId).isEqualTo("creator_id")
            assertThat(responseBody.links.hasSize(1)).isTrue()
            assertThat(responseBody.links.getLink("updateProject")).isEmpty
            coVerify { service.findSingleProject(projectId) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given existing projects - When get project for Admin Contributor - Then handler retrieves Ok Response with Edit link`() =
        runBlockingTest {
            val projectId = "projectId"
            val mockedRequestingContributor = RequestingContributor("mockedId")

            val mockedExchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/id1-mocked").build())
            val mockedRequest: ServerRequest =
                MockServerRequest.builder()
                    .attribute(headerConfigs.contributor, mockedRequestingContributor)
                    .pathVariable("id", projectId).exchange(mockedExchange).build()
            val mockedProject =
                Project("mockedProjectName", "creator_id", "mockedId", ZoneId.systemDefault())
            coEvery { service.findSingleProject(projectId) } returns mockedProject

            val outputResponse = handler.getProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val responseBody =
                @Suppress("UNCHECKED_CAST")
                (outputResponse as EntityResponse<ProjectDto>).entity()
            assertThat(responseBody.name).isEqualTo("mockedProjectName")
            assertThat(responseBody.creatorId).isEqualTo("creator_id")
            assertThat(responseBody.links.hasSize(2)).isTrue()
            assertThat(responseBody.links.getLink("updateProject")).isNotNull
            coVerify { service.findSingleProject(projectId) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given contributor - When check if Requesting Contributor is Admin of project - Then handler retrieves Ok Response`() =
        runBlockingTest {
            val projectId = "projectId"
            val mockedRequestingContributor = RequestingContributor("mockedId")
            val mockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(routeConfigs.validateAdminUser.path).build(),
            )
            val mockedRequest: ServerRequest = MockServerRequest.builder()
                .attribute(headerConfigs.contributor, mockedRequestingContributor)
                .pathVariable("id", projectId)
                .exchange(mockedExchange).build()
            val mockedProject = Project(
                "mockedProjectName",
                "creator_id",
                "mockedId",
                ZoneId.systemDefault(),
            )
            coEvery { service.findSingleProject(projectId) } returns mockedProject

            val outputResponse = handler.validateAdminUser(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<IsAdminDto>
            val responseBody = response.entity()
            assertThat(responseBody.isAdmin).isTrue()
            coVerify { service.findSingleProject(projectId) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given contributor - When get project admin not matching contributor - Then handler retrieves Ok Response with false value`() =
        runBlockingTest {
            val projectId = "projectId"
            val mockedRequestingContributor = RequestingContributor("mockedId")
            val mockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(routeConfigs.validateAdminUser.path).build(),
            )
            val mockedRequest: ServerRequest = MockServerRequest.builder()
                .attribute(headerConfigs.contributor, mockedRequestingContributor)
                .pathVariable("id", projectId)
                .exchange(mockedExchange).build()
            val mockedProject = Project(
                "mockedProjectName",
                "creator_id",
                "otherId",
                ZoneId.systemDefault(),
            )
            coEvery { service.findSingleProject(projectId) } returns mockedProject

            val outputResponse = handler.validateAdminUser(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<IsAdminDto>
            val responseBody = response.entity()
            assertThat(responseBody.isAdmin).isFalse()
            coVerify { service.findSingleProject(projectId) }
        }
}
