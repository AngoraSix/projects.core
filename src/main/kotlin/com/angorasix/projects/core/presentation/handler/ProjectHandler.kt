package com.angorasix.projects.core.presentation.handler

import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.Attribute
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.presentation.dto.AttributeDto
import com.angorasix.projects.core.presentation.dto.ProjectDto
import kotlinx.coroutines.flow.map
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import java.net.URI
import java.time.ZoneId

/**
 * Project Handler (Controller) containing all handler functions related to Project endpoints.
 *
 * @author rozagerardo
 */
class ProjectHandler(private val service: ProjectService) {

    /**
     * Handler for the List Projects endpoint, retrieving a Flux including all persisted Projects.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun listProjects(
        @Suppress("UNUSED_PARAMETER") request: ServerRequest
    ): ServerResponse =
        service.findProjects()
            .map { it.convertToDto() }
            .let {
                ok().contentType(MediaType.APPLICATION_JSON)
                    .bodyAndAwait(it)
            }

    /**
     * Handler for the Get Single Project endpoint, retrieving a Mono with the requested Project.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getProject(request: ServerRequest): ServerResponse {
        val projectId = request.pathVariable("id")
        return service.findSingleProject(projectId)
            ?.let {
                val outputProject = it.convertToDto()
                ok().contentType(MediaType.APPLICATION_JSON)
                    .bodyValueAndAwait(outputProject)
            } ?: ServerResponse.notFound()
            .buildAndAwait()
    }

    /**
     * Handler for the Create Projects endpoint, to create a new Project entity.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun createProject(request: ServerRequest): ServerResponse {
        val project = request.awaitBody<ProjectDto>()
            .convertToDomain()
        val outputProject = service.createProject(project)
            .convertToDto()
        return created(URI.create("http://localhost:8080/gertest")).contentType(MediaType.APPLICATION_JSON)
            .bodyValueAndAwait(
                outputProject
            )
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
        createdAt
    )
}

private fun Attribute<*>.convertToDto(): AttributeDto {
    return AttributeDto(
        key,
        value.toString()
    )
}

private fun ProjectDto.convertToDomain(): Project {
    return Project(
        name ?: throw IllegalArgumentException("Project name expected"),
        "id-test",
        ZoneId.of("America/Argentina/Cordoba"),
        attributes.map { it.convertToDomain() }
            .toMutableSet(),
        requirements.map { it.convertToDomain() }
            .toMutableSet()
    )
}

private fun AttributeDto.convertToDomain(): Attribute<*> {
    return Attribute(
        key,
        value,
    )
}
