package com.angorasix.projects.core.application

import com.angorasix.commons.domain.SimpleContributor
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
    fun findProjects(
        filter: ListProjectsFilter,
        simpleContributor: SimpleContributor?,
    ): Flow<Project> = repository.findUsingFilter(filter, simpleContributor)

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
     * @param updateData the [Project] to be updated
     * @return a [Mono] with the persisted [Project]
     */
    suspend fun updateProject(
        projectId: String,
        updateData: Project,
        simpleContributor: SimpleContributor,
    ): Project? =
        repository.findByIdForContributor(
            ListProjectsFilter(
                listOf(projectId),
                simpleContributor.contributorId,
            ),
            simpleContributor,
        )?.updateWithData(updateData)?.let { repository.save(it) }

    /**
     * Method to find a single [Project] from an id.
     *
     * @param projectId [Project] id
     * @return a [Mono] with the persisted [Project]
     */
    suspend fun findSingleProject(
        projectId: String,
        simpleContributor: SimpleContributor?,
    ): Project? =
        repository.findByIdForContributor(ListProjectsFilter(listOf(projectId)), simpleContributor)

    /**
     * Method to find a single [Project] from an id.
     *
     * @param projectId [Project] id
     * @return a [Mono] with the persisted [Project]
     */
    suspend fun administeredProject(
        projectId: String,
        simpleContributor: SimpleContributor,
    ): Project? = repository.findByIdForContributor(
        ListProjectsFilter(
            listOf(projectId),
            simpleContributor.contributorId,
        ),
        simpleContributor,
    )

    private fun Project.updateWithData(other: Project): Project {
        this.name = other.name
        this.attributes = other.attributes
        this.requirements = other.requirements
        return this
    }
}
