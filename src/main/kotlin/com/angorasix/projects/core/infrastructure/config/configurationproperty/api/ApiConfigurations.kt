package com.angorasix.projects.core.infrastructure.config.configurationproperty.api

import com.angorasix.commons.infrastructure.config.configurationproperty.api.Route
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

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
    @NestedConfigurationProperty
    var projectActions: ProjectActions,
)

data class BasePathConfigs(
    val projectsCore: String,
    val baseByIdCrudRoute: String,
)

data class RoutesConfigs(
    val createProject: Route,
    val updateProject: Route,
    val validateAdminUser: Route,
    val getProject: Route,
    val listProjects: Route,
)

data class ProjectActions(
    val updateProject: String,
)
