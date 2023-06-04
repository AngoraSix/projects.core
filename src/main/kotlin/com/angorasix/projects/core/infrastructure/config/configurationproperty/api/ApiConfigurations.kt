package com.angorasix.projects.core.infrastructure.config.configurationproperty.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.http.HttpMethod

/**
 * <p>
 *  Base file containing all Service configurations.
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "configs.api")
data class ApiConfigs(
    @NestedConfigurationProperty
    var routes: RoutesConfigs,
    @NestedConfigurationProperty
    var basePaths: BasePathConfigs,
)

data class BasePathConfigs constructor(val projectsCore: String)

data class RoutesConfigs constructor(
    val baseListCrudRoute: String,
    val baseByIdCrudRoute: String,
    val createProject: Route,
    val updateProject: Route,
    val validateAdminUser: Route,
    val getProject: Route,
    val listProjects: Route,
)

data class Route(
    val name: String,
    val basePaths: List<String>,
    val method: HttpMethod,
    val path: String,
) {

    fun resolvePath(): String = basePaths.joinToString("").plus(path)
}
