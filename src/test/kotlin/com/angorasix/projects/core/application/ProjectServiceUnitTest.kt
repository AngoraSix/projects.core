package com.angorasix.projects.core.application

import com.angorasix.projects.core.domain.attribute.Attribute
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.domain.project.ProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
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
    fun `given existing projects - when request find projects - then receive projects`() = runBlockingTest {
        val mockedProject = Project(
            "mockedProjectName",
            "creator_id",
            mutableSetOf<Attribute<*>>(),
            ZoneId.systemDefault()
        )
        coEvery { repository.findAll() } returns flowOf(mockedProject)

        val outputProjects = service.findProjects()

        outputProjects.collect {
            assertThat<Project>(it).isSameAs(mockedProject)
        }
        coVerify { repository.findAll() }
    }

    @Test
    @Throws(Exception::class)
    fun givenExistingProject_whenFindSingleProjects_thenServiceRetrievesMonoWithProject() = runBlockingTest {
        val mockedProjectId = "id1"
        val mockedProject = Project(
            "mockedProjectName",
            "creator_id",
            mutableSetOf<Attribute<*>>(),
            ZoneId.systemDefault()
        )
        coEvery { repository.findById(mockedProjectId) } returns mockedProject
        val outputProject = service.findSingleProject(mockedProjectId)
        assertThat(outputProject).isSameAs(mockedProject)
        coVerify { repository.findById(mockedProjectId) }
    }

    @Test
    @Throws(Exception::class)
    fun whenCreateProject_thenServiceRetrieveSavedProject() = runBlockingTest {
        val mockedProject = Project(
            "mockedProjectName",
            "creator_id",
            mutableSetOf<Attribute<*>>(),
            ZoneId.systemDefault()
        )
        val savedProject = Project(
            "savedProjectName",
            "creator_id",
            mutableSetOf<Attribute<*>>(),
            ZoneId.systemDefault()
        )
        coEvery { repository.save(mockedProject) } returns savedProject
        val outputProject = service.createProject(mockedProject)
        assertThat(outputProject).isSameAs(savedProject)
        coVerify { repository.save(mockedProject) }
    }

    @Test
    @Throws(Exception::class)
    fun whenUpdateProject_thenServiceRetrieveUpdatedProject() = runBlockingTest {
        val mockedProject = Project(
            "mockedProjectName",
            "creator_id",
            mutableSetOf<Attribute<*>>(),
            ZoneId.systemDefault()
        )
        val updatedProject = Project(
            "updatedProjectName",
            "creator_id",
            mutableSetOf<Attribute<*>>(),
            ZoneId.systemDefault()
        )
        coEvery { repository.save(mockedProject) } returns updatedProject
        val outputProject = service.createProject(mockedProject)
        assertThat(outputProject).isSameAs(updatedProject)
        coVerify { repository.save(mockedProject) }
    }
}
