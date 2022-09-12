package com.angorasix.projects.core.application

import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.domain.project.ProjectRepository
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service providing functionality for Projects.
 *
 * @author rozagerardo
 */
class ProjectService(private val repository: ProjectRepository) {

    /**
     * Method to retrieve a collection of [Project]s.
     *
     * @return [Flux] of [Project]
     */
    fun findProjects(filter: ListProjectsFilter): Flow<Project> = repository.findUsingFilter(filter)

    /**
     * Method to create a new [Project].
     *
     * @param newProject [Project] to persist
     * @return a [Mono] with the persisted [Project]
     */
    suspend fun createProject(newProject: Project): Project = repository.save(newProject)

    /**
     * Method to update an existing [Project].
     *
     * @param updatedProject the [Project] to be updated
     * @return a [Mono] with the persisted [Project]
     */
    suspend fun updateProject(projectId: String, updateData: Project): Project? =
        repository.findById(projectId)?.updateWithData(updateData)?.let { repository.save(it) }

    /**
     * Method to find a single [Project] from an id.
     *
     * @param projectId [Project] id
     * @return a [Mono] with the persisted [Project]
     */
    suspend fun findSingleProject(projectId: String): Project? = repository.findById(projectId)

    private fun Project.updateWithData(other: Project): Project {
        this.name = other.name
        this.attributes = other.attributes
        this.requirements = other.requirements
        return this
    }
}
