package com.angorasix.projects.core.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.reactive.presentation.error.resolveBadRequest
import com.angorasix.commons.reactive.presentation.error.resolveNotFound
import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.Attribute
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import com.angorasix.projects.core.presentation.dto.AttributeDto
import com.angorasix.projects.core.presentation.dto.IsAdminDto
import com.angorasix.projects.core.presentation.dto.ProjectDto
import kotlinx.coroutines.flow.map
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.Link
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.ZoneId

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
    suspend fun listProjects(@Suppress("UNUSED_PARAMETER") request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        return service.findProjects(
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
        return service.findSingleProject(projectId, requestingContributor as SimpleContributor?)
            ?.let {
                val outputProject = it.convertToDto(
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
     * @TODO: Still used now that admins is resolved per-service?
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
            val project = request.awaitBody<ProjectDto>()
                .convertToDomain(
                    requestingContributor.contributorId,
                    setOf(SimpleContributor(requestingContributor.contributorId, emptySet())),
                )
            val outputProject = service.createProject(project)
                .convertToDto(requestingContributor, apiConfigs, request)
            created(URI.create(outputProject.links.getRequiredLink(IanaLinkRelations.SELF).href)).contentType(
                MediaTypes.HAL_FORMS_JSON,
            )
                .bodyValueAndAwait(outputProject)
        } else {
            resolveBadRequest("Invalid Contributor Header", "Contributor Header")
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
        val updateProjectData = try {
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
            service.updateProject(
                projectId,
                updateProjectData,
                requestingContributor,
            )?.let {
                val outputProject = it.convertToDto(
                    requestingContributor,
                    apiConfigs,
                    request,
                )
                ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(outputProject)
            } ?: resolveNotFound("Can't update this project", "Project")
        } else {
            resolveBadRequest("Invalid Contributor Header", "Contributor Header")
        }
    }
}

private fun Project.convertToDto(): ProjectDto {
    return ProjectDto(
        id,
        name,
        attributes.map { it.convertToDto() }.toMutableSet(),
        requirements.map { it.convertToDto() }.toMutableSet(),
        creatorId,
        private,
        admins,
        createdAt,
    )
}

private fun Project.convertToDto(
    simpleContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectDto = convertToDto().resolveHypermedia(simpleContributor, this, apiConfigs, request)

private fun Attribute<*>.convertToDto(): AttributeDto {
    return AttributeDto(key, value.toString())
}

private fun ProjectDto.convertToDomain(
    contributorId: String,
    admins: Set<SimpleContributor>,
): Project {
    return Project(
        name
            ?: throw IllegalArgumentException("Project name expected"),
        contributorId,
        admins,
        ZoneId.of("America/Argentina/Cordoba"),
        private ?: false,
        attributes.map { it.convertToDomain() }.toMutableSet(),
        requirements.map { it.convertToDomain() }.toMutableSet(),
    )
}

private fun ProjectDto.resolveHypermedia(
    simpleContributor: SimpleContributor?,
    project: Project,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectDto {
    val getSingleRoute = apiConfigs.routes.getProject
    // self
    val selfLink =
        Link.of(uriBuilder(request).path(getSingleRoute.resolvePath()).build().toUriString())
            .withRel(getSingleRoute.name).expand(id).withSelfRel()
    val selfLinkWithDefaultAffordance =
        Affordances.of(selfLink).afford(HttpMethod.OPTIONS).withName("default").toLink()
    add(selfLinkWithDefaultAffordance)

    // edit Project
    if (simpleContributor != null) {
        if (project.isAdministeredBy(simpleContributor)) {
            val editProjectRoute = apiConfigs.routes.updateProject
            val editProjectLink = Link.of(
                uriBuilder(request).path(editProjectRoute.resolvePath()).build().toUriString(),
            ).withTitle(editProjectRoute.name).withName(editProjectRoute.name)
                .withRel(editProjectRoute.name).expand(id)
            val editProjectAffordanceLink = Affordances.of(editProjectLink).afford(HttpMethod.PUT)
                .withName(editProjectRoute.name).toLink()
            add(editProjectAffordanceLink)
        }
    }
    return this
}

private fun uriBuilder(request: ServerRequest) = request.requestPath().contextPath().let {
    UriComponentsBuilder.fromHttpRequest(request.exchange().request).replacePath(it.toString()) //
        .replaceQuery("")
}

private fun AttributeDto.convertToDomain(): Attribute<*> {
    return Attribute(key, value)
}
