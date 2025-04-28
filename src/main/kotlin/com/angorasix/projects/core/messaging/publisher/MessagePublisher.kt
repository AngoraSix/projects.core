package com.angorasix.projects.core.messaging.publisher

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.intercommunication.dto.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.dto.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import com.angorasix.commons.infrastructure.intercommunication.dto.project.ProjectCreated
import com.angorasix.projects.core.infrastructure.config.configurationproperty.amqp.AmqpConfigurations
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder

class MessagePublisher(
    private val streamBridge: StreamBridge,
    private val amqpConfigs: AmqpConfigurations,
) {
    fun publishProjectCreated(
        projectCreated: ProjectCreated,
        requestingContributor: SimpleContributor,
    ) {
        streamBridge.send(
            amqpConfigs.bindings.projectCreated,
            MessageBuilder
                .withPayload(
                    A6InfraMessageDto(
                        targetId = projectCreated.projectId,
                        targetType = A6DomainResource.Project,
                        objectId = projectCreated.projectId,
                        objectType = A6DomainResource.Project.value,
                        topic = A6InfraTopics.PROJECT_CREATED.value,
                        requestingContributor = requestingContributor,
                        messageData = projectCreated,
                    ),
                ).build(),
        )
    }
}
