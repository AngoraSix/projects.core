package com.angorasix.projects.core.infrastructure.service

import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.ProjectRepository
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import com.angorasix.projects.core.presentation.router.ProjectRouter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfiguration {
    @Bean
    fun projectService(repository: ProjectRepository) = ProjectService(repository)

    @Bean
    fun projectHandler(
        service: ProjectService,
        apiConfigs: ApiConfigs,
    ) = ProjectHandler(service, apiConfigs)

    @Bean
    fun projectRouter(
        handler: ProjectHandler,
        apiConfigs: ApiConfigs,
    ) = ProjectRouter(handler, apiConfigs).projectRouterFunction()

//    @Bean
//    fun messagePublisher(
//        streamBridge: StreamBridge,
//        amqpConfigs: AmqpConfigurations,
//    ) = MessagePublisher(streamBridge, amqpConfigs)
}
