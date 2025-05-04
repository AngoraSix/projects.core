package com.angorasix.projects.core.infrastructure.applicationevents

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.projects.core.domain.project.Project

data class ProjectCreatedApplicationEvent(
    val newProject: Project,
    val requestingContributor: A6Contributor,
)
