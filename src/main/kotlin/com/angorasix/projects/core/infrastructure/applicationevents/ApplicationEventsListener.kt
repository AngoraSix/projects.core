package com.angorasix.projects.core.infrastructure.applicationevents

import com.angorasix.commons.infrastructure.intercommunication.dto.project.ProjectCreated
import com.angorasix.projects.core.messaging.publisher.MessagePublisher
import org.springframework.context.event.EventListener

class ApplicationEventsListener(
    private val messagePublisher: MessagePublisher,
) {
    @EventListener
    fun handleUpdatedAssets(projectCreatedEvent: ProjectCreatedApplicationEvent) =
        projectCreatedEvent.newProject.id?.let {
            messagePublisher.publishProjectCreated(
                projectCreated =
                    ProjectCreated(
                        projectId = projectCreatedEvent.newProject.id,
                        creatorContributor = projectCreatedEvent.requestingContributor,
                    ),
                requestingContributor = projectCreatedEvent.requestingContributor,
            )
        }
}
