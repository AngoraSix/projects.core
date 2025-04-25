package com.angorasix.projects.core.infrastructure.applicationevents

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.core.domain.project.Project

data class ProjectCreatedApplicationEvent(
    val newProject: Project,
    val requestingContributor: SimpleContributor,
)
