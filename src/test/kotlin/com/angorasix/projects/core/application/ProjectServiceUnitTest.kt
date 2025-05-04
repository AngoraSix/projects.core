package com.angorasix.projects.core.application

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.projects.core.domain.project.Attribute
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.domain.project.ProjectRepository
import com.angorasix.projects.core.infrastructure.applicationevents.ProjectCreatedApplicationEvent
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifyAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ProjectServiceUnitTest {
    private lateinit var service: ProjectService

    @MockK
    private lateinit var repository: ProjectRepository

    @MockK
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @BeforeEach
    fun init() {
        service = ProjectService(repository, applicationEventPublisher)
    }

    @Test
    @Throws(Exception::class)
    fun `given existing projects - when request find projects - then receive projects`() =
        runTest {
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(A6Contributor("creator_id")),
                )
            val filter = ListProjectsFilter()
            coEvery { repository.findUsingFilter(filter, null) } returns flowOf(mockedProject)

            val outputProjects = service.findProjects(filter, null)

            outputProjects.collect {
                assertThat<Project>(it).isSameAs(mockedProject)
            }
            coVerify { repository.findUsingFilter(filter, null) }
        }

    @Test
    @Throws(Exception::class)
    fun givenExistingProject_whenFindSingleProjects_thenServiceRetrievesMonoWithProject() =
        runTest {
            val mockedProjectId = "id1"
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(A6Contributor("creator_id")),
                )
            coEvery {
                repository.findForContributorUsingFilter(
                    ListProjectsFilter(listOf(mockedProjectId)),
                    null,
                )
            } returns mockedProject
            val outputProject = service.findSingleProject(mockedProjectId, null)
            assertThat(outputProject).isSameAs(mockedProject)
            coVerify {
                repository.findForContributorUsingFilter(
                    ListProjectsFilter(listOf(mockedProjectId)),
                    null,
                )
            }
        }

    @Test
    @Throws(Exception::class)
    fun whenCreateProject_thenServiceRetrieveSavedProject() =
        runTest {
            val mockedContributor = A6Contributor("mockedId")
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(A6Contributor("creator_id")),
                )
            val savedProject =
                Project(
                    "savedProjectName",
                    "creator_id",
                    setOf(A6Contributor("creator_id")),
                )
            coEvery { repository.save(mockedProject) } returns savedProject
            every { applicationEventPublisher.publishEvent(ofType(ProjectCreatedApplicationEvent::class)) } just Runs
            val outputProject = service.createProject(mockedProject, mockedContributor)
            assertThat(outputProject).isSameAs(savedProject)
            coVerify { repository.save(mockedProject) }
            coVerify { applicationEventPublisher.publishEvent(ofType(ProjectCreatedApplicationEvent::class)) }
        }

    @Test
    @Throws(Exception::class)
    fun whenUpdateProject_thenServiceRetrieveSavedProject() =
        runTest {
            val mockedA6Contributor = A6Contributor("mockedId")
            val mockedExistingProject = mockk<Project>()
            every {
                mockedExistingProject.setProperty(Project::name.name) value "mockedUpdatedProjectName"
            } just Runs
            every {
                mockedExistingProject.setProperty(Project::attributes.name) value emptySet<Attribute<Any>>()
            } just Runs
            every {
                mockedExistingProject.setProperty(Project::requirements.name) value emptySet<Attribute<Any>>()
            } just Runs
            val mockedUpdateProject =
                Project(
                    "mockedUpdatedProjectName",
                    "creator_id",
                    setOf(A6Contributor("creator_id")),
                )
            val savedProject =
                Project(
                    "savedProjectName",
                    "creator_id",
                    setOf(A6Contributor("creator_id")),
                )
            coEvery {
                repository.findForContributorUsingFilter(
                    ListProjectsFilter(listOf("id1"), listOf("mockedId")),
                    mockedA6Contributor,
                )
            } returns mockedExistingProject
            coEvery { repository.save(any()) } returns savedProject
            val outputProject =
                service.updateProject("id1", mockedUpdateProject, mockedA6Contributor)
            assertThat(outputProject).isSameAs(savedProject)
            coVerifyAll {
                repository.findForContributorUsingFilter(
                    ListProjectsFilter(listOf("id1"), listOf("mockedId")),
                    mockedA6Contributor,
                )
                repository.findForContributorUsingFilter(
                    ListProjectsFilter(listOf("id1"), null),
                    null,
                )
                repository.save(any())
            }
            verifyAll {
                mockedExistingProject.setProperty(Project::name.name) value "mockedUpdatedProjectName"
                mockedExistingProject.setProperty(Project::attributes.name) value emptySet<Attribute<Any>>()
                mockedExistingProject.setProperty(Project::requirements.name) value emptySet<Attribute<Any>>()
            }
            confirmVerified(mockedExistingProject, repository)
        }

    @Test
    @Throws(Exception::class)
    fun whenUpdateProject_thenServiceRetrieveUpdatedProject() =
        runTest {
            val mockedContributor = A6Contributor("mockedId")
            val mockedProject =
                Project(
                    "mockedProjectName",
                    "creator_id",
                    setOf(A6Contributor("creator_id")),
                )
            val updatedProject =
                Project(
                    "updatedProjectName",
                    "creator_id",
                    setOf(A6Contributor("creator_id")),
                )
            coEvery { repository.save(mockedProject) } returns updatedProject
            every { applicationEventPublisher.publishEvent(ofType(ProjectCreatedApplicationEvent::class)) } just Runs
            val outputProject = service.createProject(mockedProject, mockedContributor)
            assertThat(outputProject).isSameAs(updatedProject)
            coVerify { repository.save(mockedProject) }
            coVerify { applicationEventPublisher.publishEvent(ofType(ProjectCreatedApplicationEvent::class)) }
        }
}
