package com.angorasix.projects.core.presentation.handler

import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.attribute.Attribute
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
    suspend fun listProjects(request: ServerRequest): ServerResponse {
        val projects = service.findProjects()
            .map { convertProjectToDto(it) }
        return ok().contentType(MediaType.APPLICATION_JSON)
            .bodyAndAwait(projects)
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
                val outputProject = convertProjectToDto(it)
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
        val project = convertProjectToDomainObject(request.awaitBody<ProjectDto>())
        val outputProject = convertProjectToDto(service.createProject(project))
        return created(URI.create("http://localhost:8080/gertest")).contentType(MediaType.APPLICATION_JSON)
            .bodyValueAndAwait(
                outputProject
            )
    }

    companion object {
        private fun convertProjectToDto(project: Project): ProjectDto {
            return ProjectDto(
                project.id,
                project.name,
                project.attributes.map { convertAttributeToDto(it) },
                project.requirements.map { convertAttributeToDto(it) },
                project.creatorId,
                project.createdAt
            )
        }

        private fun convertProjectToDomainObject(projectDto: ProjectDto): Project {
            return Project(
                projectDto.name ?: throw IllegalArgumentException("name expected"),
                "id-test",
                ZoneId.of("America/Argentina/Cordoba")
            )
        }

        private fun convertAttributeToDto(attribute: Attribute<*>): AttributeDto {
            return AttributeDto(
                attribute.value as String,
                attribute.key
            )
        }

        private fun convertAttributeToDomainObject(attributeDto: AttributeDto): Attribute<*> {
            return Attribute(
                attributeDto.value,
                attributeDto.key
            )
        }
    }
}
