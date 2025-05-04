package com.angorasix.projects.core.messaging.publisher

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.intercommunication.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.messaging.A6InfraMessageDto
import com.angorasix.commons.infrastructure.intercommunication.project.ProjectCreated
import com.angorasix.projects.core.infrastructure.config.configurationproperty.amqp.AmqpConfigurations
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder

class MessagePublisher(
    private val streamBridge: StreamBridge,
    private val amqpConfigs: AmqpConfigurations,
) {
    fun publishProjectCreated(
        projectCreated: ProjectCreated,
        requestingContributor: A6Contributor,
    ) {
        streamBridge.send(
            amqpConfigs.bindings.projectCreated,
            MessageBuilder
                .withPayload(
                    A6InfraMessageDto(
                        targetId = projectCreated.projectId,
                        targetType = A6DomainResource.PROJECT,
                        objectId = projectCreated.projectId,
                        objectType = A6DomainResource.PROJECT.value,
                        topic = A6InfraTopics.PROJECT_CREATED.value,
                        requestingContributor = requestingContributor,
                        messageData = projectCreated,
                    ),
                ).build(),
        )
    }
}
