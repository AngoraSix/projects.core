package com.angorasix.projects.core.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.config.configurationproperty.api.Route
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.presentation.dto.IsAdminDto
import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ProjectActions
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.RoutesConfigs
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import com.angorasix.projects.core.presentation.dto.ProjectDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.mediatype.problem.Problem
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.EntityResponse
import org.springframework.web.reactive.function.server.ServerRequest

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ProjectHandlerUnitTest {
    private lateinit var handler: ProjectHandler

    @MockK
    private lateinit var service: ProjectService

    @MockK
    private lateinit var apiConfigs: ApiConfigs

    private var routeConfigs: RoutesConfigs =
        RoutesConfigs(
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

    private var projectActions = ProjectActions("updateProject")

    @BeforeEach
    fun init() {
        every { apiConfigs.routes } returns routeConfigs
        every { apiConfigs.projectActions } returns projectActions
        handler = ProjectHandler(service, apiConfigs)
    }

    @Test
    @Throws(Exception::class)
    fun `Given existing projects - When list projects - Then handler retrieves Ok Response`() =
        runTest {
            val mockedExchange =
                MockServerWebExchange.from(
                    MockServerHttpRequest.get(routeConfigs.listProjects.path).build(),
                )
            val mockedRequest: ServerRequest =
                MockServerRequest.builder().exchange(mockedExchange).build()
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(SimpleContributor("creator_id", emptySet())),
                )
            val mockedPersistedProject = spyk(mockedProject)
            every { mockedPersistedProject.id } returns "mockedId"
            val retrievedProject = flowOf(mockedPersistedProject)
            coEvery { service.findProjects(ListProjectsFilter(), null) } returns retrievedProject

            val outputResponse = handler.listProjects(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response =
                @Suppress("UNCHECKED_CAST")
                outputResponse
                    as EntityResponse<Flow<ProjectDto>>
            val responseBody = response.entity()
            responseBody.collect {
                assertThat(it.name).isEqualTo("mockedProjectName")
                assertThat(it.creatorId).isEqualTo("creator_id")
            }
            coVerify { service.findProjects(ListProjectsFilter(), null) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given existing projects - When list projects using Filter - Then handler retrieves Ok Response using filters`() =
        runTest {
            val mockedExchange =
                MockServerWebExchange.from(
                    MockServerHttpRequest.get(routeConfigs.listProjects.path).build(),
                )
            val mockedRequest: ServerRequest =
                MockServerRequest
                    .builder()
                    .queryParams(
                        generateListProjectsFilterMultiValueMap(
                            "id1,id2",
                            "true",
                            "adminId1",
                        ),
                    ).exchange(mockedExchange)
                    .build()
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(SimpleContributor("creator_id", emptySet())),
                )
            val mockedPersistedProject = spyk(mockedProject)
            every { mockedPersistedProject.id } returns "mockedId"
            val retrievedProject = flowOf(mockedPersistedProject)
            coEvery {
                service.findProjects(
                    ListProjectsFilter(
                        listOf("id1", "id2"),
                        listOf("adminId1"),
                        true,
                    ),
                    null,
                )
            } returns retrievedProject

            val outputResponse = handler.listProjects(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response =
                @Suppress("UNCHECKED_CAST")
                outputResponse
                    as EntityResponse<Flow<ProjectDto>>
            val responseBody = response.entity()
            responseBody.collect {
                assertThat(it.name).isEqualTo("mockedProjectName")
                assertThat(it.creatorId).isEqualTo("creator_id")
            }
            coVerify { service.findProjects(ListProjectsFilter(), null) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with project and RequestingContributor - When create project - Then handler retrieves Created`() =
        runBlocking {
            // = runTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectDto =
                ProjectDto(
                    null,
                    "mockedInputProjectName",
                )
            val mockedSimpleContributor = SimpleContributor("mockedId")
            val mockedExchange =
                MockServerWebExchange.from(
                    MockServerHttpRequest.get(routeConfigs.createProject.path).build(),
                )
            val mockedRequest: ServerRequest =
                MockServerRequest
                    .builder()
                    .attribute(
                        AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                        mockedSimpleContributor,
                    ).exchange(mockedExchange)
                    .body(mono { mockedProjectDto })
            val mockedProjectBase =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(SimpleContributor("creator_id", emptySet())),
                )
            val mockedPersistedProject = spyk(mockedProjectBase)
            every { mockedPersistedProject.id } returns "mockedId"
            coEvery { service.createProject(ofType(Project::class)) } returns mockedPersistedProject

            val outputResponse = handler.createProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.CREATED)
            val response =
                @Suppress("UNCHECKED_CAST")
                outputResponse
                    as EntityResponse<ProjectDto>
            val responseBody = response.entity()
            assertThat(responseBody).isNotSameAs(mockedProjectDto)
            assertThat(responseBody.name).isEqualTo("mockedProjectName")
            assertThat(responseBody.creatorId).isEqualTo("creator_id")
            coVerify { service.createProject(ofType(Project::class)) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with project and no RequestingContributor - When create project - Then handler retrieves Bad Request`() =
        runBlocking {
            // = runTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectDto =
                ProjectDto(
                    null,
                    "mockedInputProjectName",
                )
            val mockedExchange =
                MockServerWebExchange.from(
                    MockServerHttpRequest.get(routeConfigs.createProject.path).build(),
                )
            val mockedRequest: ServerRequest =
                MockServerRequest.builder().exchange(mockedExchange).body(mono { mockedProjectDto })
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(SimpleContributor("creator_id", emptySet())),
                )
            coEvery { service.createProject(ofType(Project::class)) } returns mockedProject

            val outputResponse = handler.createProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            val response =
                @Suppress("UNCHECKED_CAST")
                outputResponse
                    as EntityResponse<EntityModel<Problem.ExtendedProblem<Any>>>
            val responseBody = response.entity()
            assertThat(responseBody.content?.status).isEqualTo(HttpStatus.BAD_REQUEST)
            var properties = responseBody.content?.properties as Map<String, Any>?
            assertThat(properties?.get("errorCode") as String).isEqualTo("CONTRIBUTOR_INVALID")
            Unit
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with project and RequestingContributor - When update project - Then handler retrieves Updated`() =
        runBlocking {
            // = runTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectDto =
                ProjectDto(
                    null,
                    "mockedInputProjectName",
                )
            val mockedSimpleContributor = SimpleContributor("mockedId")
            val mockedExchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/id1-mocked").build())
            val mockedRequest: ServerRequest =
                MockServerRequest
                    .builder()
                    .attribute(
                        AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                        mockedSimpleContributor,
                    ).pathVariable("id", "id1")
                    .exchange(mockedExchange)
                    .body(mono { mockedProjectDto })
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(SimpleContributor("creator_id", emptySet())),
                )
            val mockedPersistedProject = spyk(mockedProject)
            every { mockedPersistedProject.id } returns "id1"
            coEvery {
                service.updateProject(
                    "id1",
                    ofType(Project::class),
                    mockedSimpleContributor,
                )
            } returns mockedPersistedProject

            val outputResponse = handler.updateProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response =
                @Suppress("UNCHECKED_CAST")
                outputResponse
                    as EntityResponse<ProjectDto>
            val responseBody = response.entity()
            assertThat(responseBody).isNotSameAs(mockedProjectDto)
            assertThat(responseBody.name).isEqualTo("mockedProjectName")
            assertThat(responseBody.creatorId).isEqualTo("creator_id")
            coVerify {
                service.updateProject(
                    "id1",
                    ofType(Project::class),
                    mockedSimpleContributor,
                )
            }
        }

    @Test
    @Throws(Exception::class)
    fun `Given existing projects - When get project for non Admin contributor - Then handler retrieves Ok Response without Edit link`() =
        runTest {
            val projectId = "projectId"
            val mockedSimpleContributor = SimpleContributor("mockedId")
            val mockedExchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/id1-mocked").build())
            val mockedRequest: ServerRequest =
                MockServerRequest
                    .builder()
                    .attribute(
                        AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                        mockedSimpleContributor,
                    ).pathVariable("id", projectId)
                    .exchange(mockedExchange)
                    .build()
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(SimpleContributor("other_id", emptySet())),
                )
            val mockedPersistedProject = spyk(mockedProject)
            every { mockedPersistedProject.id } returns "mockedId"
            coEvery {
                service.findSingleProject(
                    projectId,
                    mockedSimpleContributor,
                )
            } returns mockedPersistedProject

            val outputResponse = handler.getProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val responseBody =
                @Suppress("UNCHECKED_CAST")
                (outputResponse as EntityResponse<ProjectDto>).entity()
            assertThat(responseBody.name).isEqualTo("mockedProjectName")
            assertThat(responseBody.creatorId).isEqualTo("creator_id")
            assertThat(responseBody.links.hasSize(1)).isTrue()
            assertThat(responseBody.links.getLink("updateProject")).isEmpty
            coVerify { service.findSingleProject(projectId, mockedSimpleContributor) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given existing projects - When get project for Admin Contributor - Then handler retrieves Ok Response with Edit link`() =
        runTest {
            val projectId = "projectId"
            val mockedSimpleContributor = SimpleContributor("mockedId")

            val mockedExchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/id1-mocked").build())
            val mockedRequest: ServerRequest =
                MockServerRequest
                    .builder()
                    .attribute(
                        AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                        mockedSimpleContributor,
                    ).pathVariable("id", projectId)
                    .exchange(mockedExchange)
                    .build()
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(SimpleContributor("mockedId", emptySet())),
                )
            val mockedPersistedProject = spyk(mockedProject)
            every { mockedPersistedProject.id } returns "mockedId"
            coEvery {
                service.findSingleProject(
                    projectId,
                    mockedSimpleContributor,
                )
            } returns mockedPersistedProject

            val outputResponse = handler.getProject(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val responseBody =
                @Suppress("UNCHECKED_CAST")
                (outputResponse as EntityResponse<ProjectDto>).entity()
            assertThat(responseBody.name).isEqualTo("mockedProjectName")
            assertThat(responseBody.creatorId).isEqualTo("creator_id")
            assertThat(responseBody.links.hasSize(2)).isTrue()
            assertThat(responseBody.links.getLink("updateProject")).isNotNull
            coVerify { service.findSingleProject(projectId, mockedSimpleContributor) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given contributor - When check if Requesting Contributor is Admin of project - Then handler retrieves Ok Response`() =
        runTest {
            val projectId = "projectId"
            val mockedSimpleContributor = SimpleContributor("mockedId")
            val mockedExchange =
                MockServerWebExchange.from(
                    MockServerHttpRequest.get(routeConfigs.validateAdminUser.path).build(),
                )
            val mockedRequest: ServerRequest =
                MockServerRequest
                    .builder()
                    .attribute(
                        AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                        mockedSimpleContributor,
                    ).pathVariable("id", projectId)
                    .exchange(mockedExchange)
                    .build()
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(SimpleContributor("mockedId", emptySet())),
                )
            coEvery {
                service.administeredProject(
                    projectId,
                    mockedSimpleContributor,
                )
            } returns mockedProject

            val outputResponse = handler.validateAdminUser(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response =
                @Suppress("UNCHECKED_CAST")
                outputResponse
                    as EntityResponse<IsAdminDto>
            val responseBody = response.entity()
            assertThat(responseBody.isAdmin).isTrue()
            coVerify { service.administeredProject(projectId, mockedSimpleContributor) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given contributor - When get project admin not matching contributor - Then handler retrieves Ok Response with false value`() =
        runTest {
            val projectId = "projectId"
            val mockedSimpleContributor = SimpleContributor("mockedId")
            val mockedExchange =
                MockServerWebExchange.from(
                    MockServerHttpRequest.get(routeConfigs.validateAdminUser.path).build(),
                )
            val mockedRequest: ServerRequest =
                MockServerRequest
                    .builder()
                    .attribute(
                        AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                        mockedSimpleContributor,
                    ).pathVariable("id", projectId)
                    .exchange(mockedExchange)
                    .build()
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(SimpleContributor("otherId", emptySet())),
                )
            coEvery {
                service.administeredProject(
                    projectId,
                    mockedSimpleContributor,
                )
            } returns mockedProject

            val outputResponse = handler.validateAdminUser(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response =
                @Suppress("UNCHECKED_CAST")
                outputResponse
                    as EntityResponse<IsAdminDto>
            val responseBody = response.entity()
            assertThat(responseBody.isAdmin).isFalse()
            coVerify { service.administeredProject(projectId, mockedSimpleContributor) }
        }

    fun generateListProjectsFilterMultiValueMap(
        ids: String,
        private: String,
        adminId: String,
    ): MultiValueMap<String, String> {
        val multiMap: MultiValueMap<String, String> = LinkedMultiValueMap()
        multiMap.add("ids", ids)
        multiMap.add("private", private)
        multiMap.add("adminId", adminId)
        return multiMap
    }
}
