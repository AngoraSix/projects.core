package com.angorasix.projects.core.presentation.handler

import com.angorasix.commons.domain.RequestingContributor
import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.Attribute
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.config.api.ApiConfigs
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import com.angorasix.projects.core.presentation.dto.AttributeDto
import com.angorasix.projects.core.presentation.dto.IsAdminDto
import com.angorasix.projects.core.presentation.dto.ProjectDto
import kotlinx.coroutines.flow.map
import org.springframework.hateoas.Link
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
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
    suspend fun listProjects(
            @Suppress("UNUSED_PARAMETER") request: ServerRequest
    ): ServerResponse {
        val requestingContributor = request.attributes()[apiConfigs.headers.contributor]
        return service.findProjects(request.queryParams().toQueryFilter())
                .map { it.convertToDto(requestingContributor as? RequestingContributor, apiConfigs, request) }
                .let {
                    ok().contentType(MediaType.APPLICATION_JSON)
                            .bodyAndAwait(it)
                }
    }

    /**
     * Handler for the Get Single Project endpoint, retrieving a Mono with the requested Project.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getProject(request: ServerRequest): ServerResponse {
        val requestingContributor = request.attributes()[apiConfigs.headers.contributor]
        val projectId = request.pathVariable("id")
        return service.findSingleProject(projectId)
                ?.let {
                    val outputProject = it.convertToDto(requestingContributor as? RequestingContributor, apiConfigs, request)
                    ok().contentType(MediaType.APPLICATION_JSON)
                            .bodyValueAndAwait(outputProject)
                } ?: ServerResponse.notFound()
                .buildAndAwait()
    }

    /**
     * Handler for the Get Single Project endpoint, retrieving a Mono with the requested Project.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun validateAdminUser(request: ServerRequest): ServerResponse {
        val requestingContributor = request.attributes()[apiConfigs.headers.contributor]
        val projectId = request.pathVariable("id")
        return if (requestingContributor is RequestingContributor) {
            service.findSingleProject(projectId)
                    ?.let {
                        val result = it.adminId == requestingContributor.id
                        ok().contentType(MediaType.APPLICATION_JSON)
                                .bodyValueAndAwait(IsAdminDto(result))
                    } ?: ServerResponse.notFound().buildAndAwait()
        } else {
            badRequest().buildAndAwait()
        }
    }

    /**
     * Handler for the Create Projects endpoint, to create a new Project entity.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun createProject(request: ServerRequest): ServerResponse {
        val requestingContributor = request.attributes()[apiConfigs.headers.contributor]
        return if (requestingContributor is RequestingContributor) {
            val project = request.awaitBody<ProjectDto>()
                    .convertToDomain(requestingContributor.id, requestingContributor.id)
            val outputProject = service.createProject(project)
                    .convertToDto(requestingContributor, apiConfigs, request)
            created(URI.create("http://localhost:8080/gertest")).contentType(MediaType.APPLICATION_JSON)
                    .bodyValueAndAwait(
                            outputProject
                    )
        } else {
            badRequest().buildAndAwait()
        }
    }

    /**
     * Handler for the Update Project endpoint, retrieving a Mono with the updated Project.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun updateProject(request: ServerRequest): ServerResponse {
        val requestingContributor = request.attributes()[apiConfigs.headers.contributor]
        val projectId = request.pathVariable("id")
        val updateProjectData = request.awaitBody<ProjectDto>().let { it.convertToDomain(it.creatorId ?: "", it.adminId ?: "") }
        return service.updateProject(projectId, updateProjectData)
                ?.let {
                    val outputProject = it.convertToDto(requestingContributor as? RequestingContributor, apiConfigs, request)
                    ok().contentType(MediaType.APPLICATION_JSON)
                            .bodyValueAndAwait(outputProject)
                } ?: ServerResponse.notFound()
                .buildAndAwait()
    }
}

private fun Project.convertToDto(): ProjectDto {
    return ProjectDto(
            id,
            name,
            attributes.map { it.convertToDto() }
                    .toMutableSet(),
            requirements.map { it.convertToDto() }
                    .toMutableSet(),
            creatorId,
            adminId,
            createdAt
    )
}

private fun Project.convertToDto(requestingContributor: RequestingContributor?, apiConfigs: ApiConfigs, request: ServerRequest): ProjectDto =
        convertToDto().resolveHypermedia(requestingContributor, this, apiConfigs, request);

private fun Attribute<*>.convertToDto(): AttributeDto {
    return AttributeDto(
            key,
            value.toString()
    )
}

private fun ProjectDto.convertToDomain(contributorId: String, adminId: String): Project {
    return Project(
            name ?: throw IllegalArgumentException("Project name expected"),
            contributorId,
            adminId,
            ZoneId.of("America/Argentina/Cordoba"),
            attributes.map { it.convertToDomain() }
                    .toMutableSet(),
            requirements.map { it.convertToDomain() }
                    .toMutableSet()
    )
}

private fun ProjectDto.resolveHypermedia(requestingContributor: RequestingContributor?, project: Project, apiConfigs: ApiConfigs, request: ServerRequest): ProjectDto {
    val getSingleRoute = apiConfigs.routes.getProject
    // self
    val selfLink = Link.of(uriBuilder(request).path(getSingleRoute.resolvePath()).build().toUriString()).withRel(getSingleRoute.name).expand(id).withSelfRel()
    val selfLinkWithDefaultAffordance = Affordances.of(selfLink).afford(HttpMethod.OPTIONS).withName("default").toLink()
    add(selfLinkWithDefaultAffordance)

    // edit Project
    if (requestingContributor != null) {
        if (project.canEdit(requestingContributor)) {
            val editProjectRoute = apiConfigs.routes.updateProject
            val editProjectLink = Link.of(uriBuilder(request).path(editProjectRoute.resolvePath()).build().toUriString()).withTitle(editProjectRoute.name).withName(editProjectRoute.name).withRel(editProjectRoute.name).expand(id)
            val editProjectAffordanceLink = Affordances.of(editProjectLink).afford(HttpMethod.PUT).withName(editProjectRoute.name).toLink()
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
    return Attribute(
            key,
            value,
    )
}

private fun MultiValueMap<String, String>.toQueryFilter(): ListProjectsFilter {
    return ListProjectsFilter(get("ids")?.flatMap { it.split(",") }, getFirst("adminId"))
}
