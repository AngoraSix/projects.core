package com.angorasix.projects.core.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.presentation.dto.IsAdminDto
import com.angorasix.commons.reactive.presentation.error.resolveBadRequest
import com.angorasix.commons.reactive.presentation.error.resolveNotFound
import com.angorasix.commons.reactive.presentation.mappings.addLink
import com.angorasix.commons.reactive.presentation.mappings.addSelfLink
import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.Attribute
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import com.angorasix.projects.core.presentation.dto.AttributeDto
import com.angorasix.projects.core.presentation.dto.ProjectDto
import kotlinx.coroutines.flow.map
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.MediaTypes
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.net.URI

/**
 * Project Handler (Controller) containing all handler functions related to Project endpoints.
 *
 * @author rozagerardo
 */
class ProjectHandler(
    private val service: ProjectService,
    private val apiConfigs: ApiConfigs,
) {
    /**
     * Handler for the List Projects endpoint, retrieving a Flux including all persisted Projects.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun listProjects(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        return service
            .findProjects(
                ListProjectsFilter.fromMultiValueMap(
                    request.queryParams(),
                ),
                requestingContributor as SimpleContributor?,
            ).map {
                it.convertToDto(
                    requestingContributor,
                    apiConfigs,
                    request,
                )
            }.let {
                ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyAndAwait(it)
            }
    }

    /**
     * Handler for the Get Single Project endpoint, retrieving a Mono with the requested Project.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getProject(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        val projectId = request.pathVariable("id")
        return service
            .findSingleProject(projectId, requestingContributor as SimpleContributor?)
            ?.let {
                val outputProject =
                    it.convertToDto(
                        requestingContributor,
                        apiConfigs,
                        request,
                    )
                ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(outputProject)
            } ?: resolveNotFound("Can't find Project", "Project")
    }

    /**
     * Handler for the Get Single Project endpoint,
     * retrieving a Mono indicating whether the user is admin of the Project.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun validateAdminUser(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        val projectId = request.pathVariable("id")
        return if (requestingContributor is SimpleContributor) {
            service.administeredProject(projectId, requestingContributor)?.let {
                val result = it.isAdministeredBy(requestingContributor)
                ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(IsAdminDto(result))
            } ?: resolveNotFound("Can't find project", "Project")
        } else {
            resolveBadRequest("Invalid Contributor Authentication", "Authentication")
        }
    }

    /**
     * Handler for the Create Projects endpoint, to create a new Project entity.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun createProject(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        return if (requestingContributor is SimpleContributor) {
            val project =
                request
                    .awaitBody<ProjectDto>()
                    .convertToDomain(
                        requestingContributor.contributorId,
                        setOf(SimpleContributor(requestingContributor.contributorId, emptySet())),
                    )
            val outputProject =
                service
                    .createProject(project)
                    .convertToDto(requestingContributor, apiConfigs, request)
            created(URI.create(outputProject.links.getRequiredLink(IanaLinkRelations.SELF).href))
                .contentType(
                    MediaTypes.HAL_FORMS_JSON,
                ).bodyValueAndAwait(outputProject)
        } else {
            resolveBadRequest("Invalid Contributor", "Contributor")
        }
    }

    /**
     * Handler for the Update Project endpoint, retrieving a Mono with the updated Project.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun updateProject(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        val projectId = request.pathVariable("id")
        val updateProjectData =
            try {
                request.awaitBody<ProjectDto>().let {
                    it.convertToDomain(it.creatorId ?: "", it.admins ?: emptySet())
                }
            } catch (e: IllegalArgumentException) {
                return resolveBadRequest(
                    e.message ?: "Incorrect Project body",
                    "Project Presentation",
                )
            }
        return if (requestingContributor is SimpleContributor) {
            service
                .updateProject(
                    projectId,
                    updateProjectData,
                    requestingContributor,
                )?.let {
                    val outputProject =
                        it.convertToDto(
                            requestingContributor,
                            apiConfigs,
                            request,
                        )
                    ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(outputProject)
                } ?: resolveNotFound("Can't update this project", "Project")
        } else {
            resolveBadRequest("Invalid Contributor", "Contributor")
        }
    }
}

private fun Project.convertToDto(): ProjectDto =
    ProjectDto(
        id,
        name,
        attributes.map { it.convertToDto() }.toMutableSet(),
        requirements.map { it.convertToDto() }.toMutableSet(),
        creatorId,
        private,
        admins,
        createdInstant,
    )

private fun Project.convertToDto(
    simpleContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectDto = convertToDto().resolveHypermedia(simpleContributor, this, apiConfigs, request)

private fun Attribute<*>.convertToDto(): AttributeDto = AttributeDto(key, value.toString())

private fun ProjectDto.convertToDomain(
    contributorId: String,
    admins: Set<SimpleContributor>,
): Project =
    Project(
        name
            ?: throw IllegalArgumentException("Project name expected"),
        contributorId,
        admins,
        private ?: false,
        attributes.map { it.convertToDomain() }.toMutableSet(),
        requirements.map { it.convertToDomain() }.toMutableSet(),
    )

private fun ProjectDto.resolveHypermedia(
    simpleContributor: SimpleContributor?,
    project: Project,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectDto {
    val getSingleRoute = apiConfigs.routes.getProject
    // self
    requireNotNull(id)
    addSelfLink(getSingleRoute, request, listOf(id))

    // edit Project
    if (simpleContributor != null) {
        if (project.isAdministeredBy(simpleContributor)) {
            addLink(apiConfigs.routes.updateProject, apiConfigs.projectActions.updateProject, request, listOf(id))
        }
    }
    return this
}

private fun AttributeDto.convertToDomain(): Attribute<*> = Attribute(key, value)
