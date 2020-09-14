package com.angorasix.projects.core.application

import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.domain.project.ProjectRepository
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
    fun findProjects(): Flow<Project> {
        return repository.findAll()
    }

    /**
     * Method to create a new [Project].
     *
     * @param newProject [Project] to persist
     * @return a [Mono] with the persisted [Project]
     */
    suspend fun createProject(newProject: Project): Project {
        return repository.save(newProject)
    }

    /**
     * Method to update an existing [Project].
     *
     * @param updatedProject the [Project] to be updated
     * @return a [Mono] with the persisted [Project]
     */
    suspend fun updateProject(updatedProject: Project): Project {
        return repository.save(updatedProject)
    }

    /**
     * Method to find a single [Project] from an id.
     *
     * @param projectId [Project] id
     * @return a [Mono] with the persisted [Project]
     */
    suspend fun findSingleProject(projectId: String): Project? {
        return repository.findById(projectId)
    }
}
