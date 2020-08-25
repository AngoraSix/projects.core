package com.angorasix.projects.core.presentation.handler

import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.presentation.dto.ProjectDto
import kotlinx.coroutines.flow.map
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
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
        val projects = service.findProjects().map { convertToDto(it) }
        return ok().contentType(MediaType.APPLICATION_JSON).bodyAndAwait(projects)
    }

    /**
     * Handler for the Create Projects endpoint, to create a new Project entity.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun createProject(request: ServerRequest): ServerResponse {
//        request.bodyToMono(ProjectDto::class.java).map {
//            val asd1= convertToDomainObject(it)
//            val asd2 = convertToDto(service.createProject(asd1))
//            created(URI.create("http://localhost:8080/gertest")).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(
//                                    asd2)
//        }
//        // TODO: obtain project and Zone info
//        val project = convertToDomainObject(request.awaitBody<ProjectDto>())
////        val asd = request.bodyToMono(ProjectDto::class.java)
////        val project = asd.map { convertToDomainObject(it) }
//        val outputProject = convertToDto(service.createProject(project))
//        // TODO HATEOAS for location header
//        return created(URI.create("http://localhost:8080/gertest")).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(
//                outputProject)

        val project = request.awaitBody<ProjectDto>()

        return created(URI.create("http://localhost:8080/gertest")).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(
                project)
    }

    /**
     * Handler for the Get Single Project endpoint, retrieving a Mono with the requested Project.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getProject(request: ServerRequest): ServerResponse {
        val projectId = request.pathVariable("id")
        return service.findSingleProject(projectId)?.let {
            val outputProject = convertToDto(it)
            // TODO HATEOAS for location header
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(outputProject)
        } ?: ServerResponse.notFound().buildAndAwait()
    }

    companion object {
        // TODO: revisit these converters
        private fun convertToDto(project: Project): ProjectDto {
            return ProjectDto(project.id, project.name, project.attributes, project.requirements, project.creatorId)
        }

        private fun convertToDomainObject(projectDto: ProjectDto): Project {
            // TODO manage ZoneId and contributor
            return Project(projectDto.name ?: throw IllegalArgumentException("name expected"),
                    "id-test",
                    ZoneId.of("America/Argentina/Cordoba"))
        }
    }

}