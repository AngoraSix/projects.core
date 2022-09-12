package com.angorasix.projects.core.presentation.router

import com.angorasix.commons.presentation.filter.resolveRequestingContributor
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.reactive.function.server.CoRouterFunctionDsl
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.coRouter

/**
 * Router for all Project related endpoints.
 *
 * @author rozagerardo
 */
class ProjectRouter(
    private val handler: ProjectHandler,
    private val objectMapper: ObjectMapper,
    private val apiConfigs: ApiConfigs,
) {

    /**
     * Main RouterFunction configuration for all endpoints related to Projects.
     *
     * @return the [RouterFunction] with all the routes for Projects
     */
    fun projectRouterFunction() = coRouter {
        apiConfigs.basePaths.projectsCore.nest {
            defineValidateAdminUserEndpoint()
            apiConfigs.routes.baseByIdCrudRoute.nest {
                defineUpdateProjectEndpoint()
                defineGetProjectEndpoint()
            }
            apiConfigs.routes.baseListCrudRoute.nest {
                defineCreateProjectEndpoint()
                defineListProjectsEndpoint()
            }
        }
    }

    private fun CoRouterFunctionDsl.defineValidateAdminUserEndpoint() {
        path(apiConfigs.routes.validateAdminUser.path).nest {
            filter { request, next ->
                resolveRequestingContributor(
                    request,
                    next,
                    apiConfigs.headers.contributor,
                    objectMapper,
                )
            }
            method(apiConfigs.routes.validateAdminUser.method, handler::validateAdminUser)
        }
    }

    private fun CoRouterFunctionDsl.defineUpdateProjectEndpoint() {
        method(apiConfigs.routes.updateProject.method).nest {
            filter { request, next ->
                resolveRequestingContributor(
                    request,
                    next,
                    apiConfigs.headers.contributor,
                    objectMapper,
                )
            }
            method(apiConfigs.routes.updateProject.method, handler::updateProject)
        }
    }

    private fun CoRouterFunctionDsl.defineGetProjectEndpoint() {
        method(apiConfigs.routes.getProject.method).nest {
            filter { request, next ->
                resolveRequestingContributor(
                    request,
                    next,
                    apiConfigs.headers.contributor,
                    objectMapper,
                    true,
                )
            }
            method(apiConfigs.routes.getProject.method, handler::getProject)
        }
    }

    private fun CoRouterFunctionDsl.defineCreateProjectEndpoint() {
        method(apiConfigs.routes.createProject.method).nest {
            filter { request, next ->
                resolveRequestingContributor(
                    request,
                    next,
                    apiConfigs.headers.contributor,
                    objectMapper,
                )
            }
            method(apiConfigs.routes.createProject.method, handler::createProject)
        }
    }

    private fun CoRouterFunctionDsl.defineListProjectsEndpoint() {
        method(apiConfigs.routes.listProjects.method).nest {
            filter { request, next ->
                resolveRequestingContributor(
                    request,
                    next,
                    apiConfigs.headers.contributor,
                    objectMapper,
                    true,
                )
            }
            method(apiConfigs.routes.listProjects.method, handler::listProjects)
        }
    }
}
