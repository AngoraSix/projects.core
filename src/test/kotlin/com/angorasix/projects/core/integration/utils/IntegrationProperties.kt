package com.angorasix.projects.core.integration.utils

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 *
 *
 * @author rozagerardo
 */
@ConstructorBinding
@ConfigurationProperties("integration")
class IntegrationProperties(val mongodb: MongodbProperties) {
    data class MongodbProperties(val baseJsonFile: String)
}
