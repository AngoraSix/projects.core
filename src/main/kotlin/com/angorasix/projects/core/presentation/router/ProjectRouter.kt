package com.angorasix.projects.core.presentation.router

import com.angorasix.projects.core.infrastructure.config.api.ApiConfigs
import com.angorasix.projects.core.presentation.filter.headerFilterFunction
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.coRouter

/**
 * Router for all Project related endpoints.
 *
 * @author rozagerardo
 */
class ProjectRouter(private val handler: ProjectHandler,
                    private val objectMapper: ObjectMapper,
                    private val apiConfigs: ApiConfigs) {

    /**
     * Main RouterFunction configuration for all endpoints related to Projects.
     *
     * @return the [RouterFunction] with all the routes for Projects
     */
    fun projectRouterFunction() = coRouter {
        apiConfigs.basePaths.projectsCore.nest {
            path(apiConfigs.routes.validateAdminUser.path).nest {
                filter { request, next ->
                    headerFilterFunction(request, next, apiConfigs, objectMapper)
                }
                method(apiConfigs.routes.validateAdminUser.method, handler::validateAdminUser)
            }
            apiConfigs.routes.baseByIdCrudRoute.nest {
                method(apiConfigs.routes.updateProject.method).nest {
                    filter { request, next ->
                        headerFilterFunction(request, next, apiConfigs, objectMapper)
                    }
                    method(apiConfigs.routes.updateProject.method, handler::updateProject)
                }
                method(apiConfigs.routes.getProject.method).nest {
                    filter { request, next ->
                        headerFilterFunction(request, next, apiConfigs, objectMapper, true)
                    }
                    method(apiConfigs.routes.getProject.method, handler::getProject)
                }
            }
            apiConfigs.routes.baseListCrudRoute.nest {
                path(apiConfigs.routes.baseListCrudRoute).nest {
                    method(apiConfigs.routes.createProject.method).nest {
                        filter { request, next ->
                            headerFilterFunction(request, next, apiConfigs, objectMapper)
                        }
                        method(apiConfigs.routes.createProject.method, handler::createProject)
                    }
                    method(apiConfigs.routes.listProjects.method).nest {
                        filter { request, next ->
                            headerFilterFunction(request, next, apiConfigs, objectMapper, true)
                        }
                        method(apiConfigs.routes.listProjects.method, handler::listProjects)
                    }
                }
            }
        }
    }
}
