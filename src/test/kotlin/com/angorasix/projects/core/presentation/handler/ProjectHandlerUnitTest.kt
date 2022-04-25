package com.angorasix.projects.core.presentation.handler

import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.config.ApiConfigs
import com.angorasix.projects.core.infrastructure.config.ServiceConfigs
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import com.angorasix.projects.core.presentation.dto.ProjectDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.EntityResponse
import org.springframework.web.reactive.function.server.ServerRequest
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
class ProjectHandlerUnitTest {

    private lateinit var handler: ProjectHandler

    @MockK
    private lateinit var service: ProjectService

    private lateinit var serviceConfigs: ServiceConfigs

    @BeforeEach
    fun init() {
        serviceConfigs = ServiceConfigs()
        serviceConfigs.api = ApiConfigs("MockedContributorHeader")
        handler = ProjectHandler(service, serviceConfigs)
    }

    @Test
    @Throws(Exception::class)
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun `Given existing projects - When list projects - Then handler retrieves Ok Response`() = runBlockingTest {
        val mockedRequest: ServerRequest = MockServerRequest.builder()
                .build()
        val mockedProject = Project(
                "mockedProjectName",
                "creator_id",
                "creator_id",
                ZoneId.systemDefault()
        )
        val retrievedProject = flowOf(mockedProject)
        coEvery { service.findProjects(ListProjectsFilter()) } returns retrievedProject

        val outputResponse = handler.listProjects(mockedRequest)

        assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
        val responseBody = (@Suppress("UNCHECKED_CAST") (outputResponse as EntityResponse<Flow<ProjectDto>>)).entity()
        responseBody.collect {
            assertThat(it.name).isEqualTo("mockedProjectName")
            assertThat(it.creatorId).isEqualTo("creator_id")
        }
        coVerify { service.findProjects(ListProjectsFilter()) }
    }

    @Test
    @Throws(Exception::class)
    fun `Given request with project - When create project - Then handler retrieves Created`() =
            runBlocking { // = runBlockingTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
                val mockedProjectDto = ProjectDto(
                        null,
                        "mockedInputProjectName",
                )
                val mockedRequest: ServerRequest = MockServerRequest.builder()
                        .body(mono { mockedProjectDto })
                val mockedProject = Project(
                        "mockedProjectName",
                        "creator_id",
                        "creator_id",
                        ZoneId.systemDefault(),
                )
                coEvery { service.createProject(ofType(Project::class)) } returns mockedProject

                val outputResponse = handler.createProject(mockedRequest)

                assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.CREATED)
                val responseBody = (@Suppress("UNCHECKED_CAST") (outputResponse as EntityResponse<ProjectDto>)).entity()
                assertThat(responseBody).isNotSameAs(mockedProjectDto)
                assertThat(responseBody.name).isEqualTo("mockedProjectName")
                assertThat(responseBody.creatorId).isEqualTo("creator_id")
                coVerify { service.createProject(ofType(Project::class)) }
            }

    @Test
    @Throws(Exception::class)
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun `Given existing projects - When get project - Then handler retrieves Ok Response`() = runBlockingTest {
        val projectId = "projectId"
        val mockedRequest: ServerRequest = MockServerRequest.builder()
                .pathVariable(
                        "id",
                        projectId
                )
                .build()
        val mockedProject = Project(
                "mockedProjectName",
                "creator_id",
                "creator_id",
                ZoneId.systemDefault()
        )
        coEvery { service.findSingleProject(projectId) } returns mockedProject

        val outputResponse = handler.getProject(mockedRequest)

        assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
        val responseBody = @Suppress("UNCHECKED_CAST") (outputResponse as EntityResponse<ProjectDto>).entity()
        assertThat(responseBody.name).isEqualTo("mockedProjectName")
        assertThat(responseBody.creatorId).isEqualTo("creator_id")
        coVerify { service.findSingleProject(projectId) }
    }
}
