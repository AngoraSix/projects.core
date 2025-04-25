package com.angorasix.projects.core.infrastructure.service

import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.domain.project.ProjectRepository
import com.angorasix.projects.core.infrastructure.applicationevents.ApplicationEventsListener
import com.angorasix.projects.core.infrastructure.config.configurationproperty.amqp.AmqpConfigurations
import com.angorasix.projects.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.core.messaging.publisher.MessagePublisher
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import com.angorasix.projects.core.presentation.router.ProjectRouter
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfiguration {
    @Bean
    fun projectService(
        repository: ProjectRepository,
        applicationEventPublisher: ApplicationEventPublisher,
    ) = ProjectService(repository, applicationEventPublisher)

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

    @Bean
    fun messagePublisher(
        streamBridge: StreamBridge,
        amqpConfigs: AmqpConfigurations,
    ) = MessagePublisher(streamBridge, amqpConfigs)

    @Bean
    fun applicationEventsListener(messagePublisher: MessagePublisher) = ApplicationEventsListener(messagePublisher)
}
