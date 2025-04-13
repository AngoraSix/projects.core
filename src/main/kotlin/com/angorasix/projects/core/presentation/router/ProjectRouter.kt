package com.angorasix.projects.core.presentation.router

import com.angorasix.commons.reactive.presentation.filter.extractRequestingContributor
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.coRouter

/**
 * Router for all Project related endpoints.
 *
 * @author rozagerardo
 */
class ProjectRouter(
    private val handler: ProjectHandler,
    private val apiConfigs: ApiConfigs,
) {
    /**
     * Main RouterFunction configuration for all endpoints related to Projects.
     *
     * @return the [RouterFunction] with all the routes for Projects
     */
    fun projectRouterFunction() =
        coRouter {
            apiConfigs.basePaths.projectsCore.nest {
                filter { request, next ->
                    extractRequestingContributor(
                        request,
                        next,
                    )
                }
                apiConfigs.basePaths.baseByIdCrudRoute.nest {
                    path(apiConfigs.routes.validateAdminUser.path).nest {
                        method(apiConfigs.routes.validateAdminUser.method, handler::validateAdminUser)
                    }
                    method(apiConfigs.routes.getProject.method, handler::getProject)
                    method(apiConfigs.routes.updateProject.method, handler::updateProject)
                }
                method(apiConfigs.routes.listProjects.method, handler::listProjects)
                method(apiConfigs.routes.createProject.method, handler::createProject)
            }
        }
}
