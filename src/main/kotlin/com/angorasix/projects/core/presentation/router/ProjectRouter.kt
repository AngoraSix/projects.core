package com.angorasix.projects.core.presentation.router

import com.angorasix.projects.core.infrastructure.config.ServiceConfigs
import com.angorasix.projects.core.presentation.filter.headerFilterFunction
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.coRouter

/**
 * Router for all Project related endpoints.
 *
 * @author rozagerardo
 */
class ProjectRouter(private val handler: ProjectHandler,
                    private val objectMapper: ObjectMapper,
                    private val serviceConfigs: ServiceConfigs) {

    /**
     * Main RouterFunction configuration for all endpoints related to Projects.
     *
     * @return the [RouterFunction] with all the routes for Projects
     */
    fun projectRouterFunction() = coRouter {

        "/projects-core".nest {
            accept(APPLICATION_JSON).nest {
                GET(
                    "/{id}",
                    handler::getProject
                )
                GET(
                    "",
                    handler::listProjects
                )
                POST(
                    "",
                    handler::createProject
                )
            }
        }
        filter{ request, next ->
            headerFilterFunction(request, next, serviceConfigs, objectMapper)
        }
    }
}
