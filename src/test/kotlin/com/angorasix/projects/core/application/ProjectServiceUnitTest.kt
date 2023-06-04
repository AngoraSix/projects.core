package com.angorasix.projects.core.application

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.core.domain.project.Attribute
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.domain.project.ProjectRepository
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
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ProjectServiceUnitTest {
    private lateinit var service: ProjectService

    @MockK
    private lateinit var repository: ProjectRepository

    @BeforeEach
    fun init() {
        service = ProjectService(repository)
    }

    @Test
    @Throws(Exception::class)
    fun `given existing projects - when request find projects - then receive projects`() =
        runTest {
            val mockedProject = Project(
                "mockedProjectName",
                "creator_id",
                setOf(SimpleContributor("creator_id", emptySet())),
                ZoneId.systemDefault(),
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
            val mockedProject = Project(
                "mockedProjectName",
                "creator_id",
                setOf(SimpleContributor("creator_id", emptySet())),
                ZoneId.systemDefault(),
            )
            coEvery {
                repository.findByIdForContributor(
                    ListProjectsFilter(listOf(mockedProjectId)),
                    null,
                )
            } returns mockedProject
            val outputProject = service.findSingleProject(mockedProjectId, null)
            assertThat(outputProject).isSameAs(mockedProject)
            coVerify {
                repository.findByIdForContributor(
                    ListProjectsFilter(listOf(mockedProjectId)),
                    null,
                )
            }
        }

    @Test
    @Throws(Exception::class)
    fun whenCreateProject_thenServiceRetrieveSavedProject() = runTest {
        val mockedProject = Project(
            "mockedProjectName",
            "creator_id",
            setOf(SimpleContributor("creator_id", emptySet())),
            ZoneId.systemDefault(),
        )
        val savedProject = Project(
            "savedProjectName",
            "creator_id",
            setOf(SimpleContributor("creator_id", emptySet())),
            ZoneId.systemDefault(),
        )
        coEvery { repository.save(mockedProject) } returns savedProject
        val outputProject = service.createProject(mockedProject)
        assertThat(outputProject).isSameAs(savedProject)
        coVerify { repository.save(mockedProject) }
    }

    @Test
    @Throws(Exception::class)
    fun whenUpdateProject_thenServiceRetrieveSavedProject() = runTest {
        val mockedSimpleContributor = SimpleContributor("mockedId")
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
        val mockedUpdateProject = Project(
            "mockedUpdatedProjectName",
            "creator_id",
            setOf(SimpleContributor("creator_id", emptySet())),
            ZoneId.systemDefault(),
        )
        val savedProject = Project(
            "savedProjectName",
            "creator_id",
            setOf(SimpleContributor("creator_id", emptySet())),
            ZoneId.systemDefault(),
        )
        coEvery {
            repository.findByIdForContributor(
                ListProjectsFilter(listOf("id1"), "mockedId"),
                mockedSimpleContributor,
            )
        } returns mockedExistingProject
        coEvery { repository.save(any()) } returns savedProject
        val outputProject =
            service.updateProject("id1", mockedUpdateProject, mockedSimpleContributor)
        assertThat(outputProject).isSameAs(savedProject)
        coVerifyAll {
            repository.findByIdForContributor(
                ListProjectsFilter(listOf("id1"), "mockedId"),
                mockedSimpleContributor,
            )
            repository.findByIdForContributor(
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
    fun whenUpdateProject_thenServiceRetrieveUpdatedProject() = runTest {
        val mockedProject = Project(
            "mockedProjectName",
            "creator_id",
            setOf(SimpleContributor("creator_id", emptySet())),
            ZoneId.systemDefault(),
        )
        val updatedProject = Project(
            "updatedProjectName",
            "creator_id",
            setOf(SimpleContributor("creator_id", emptySet())),
            ZoneId.systemDefault(),
        )
        coEvery { repository.save(mockedProject) } returns updatedProject
        val outputProject = service.createProject(mockedProject)
        assertThat(outputProject).isSameAs(updatedProject)
        coVerify { repository.save(mockedProject) }
    }
}
