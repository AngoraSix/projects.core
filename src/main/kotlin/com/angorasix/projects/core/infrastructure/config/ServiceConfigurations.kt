package com.angorasix.projects.core.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

/**
 * <p>
 *  Base file containing all Service configurations.
 * </p>
 *
 * @author rozagerardo
 */
@Configuration
@ConfigurationProperties(prefix = "configs")
class ServiceConfigs {
    lateinit var api: ApiConfigs
}

@ConstructorBinding
class ApiConfigs(var contributorHeader: String) {
}