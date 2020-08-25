package com.angorasix.projects.core.presentation.dto

import com.angorasix.projects.core.domain.Attribute
import com.angorasix.projects.core.domain.project.Requirement
import org.springframework.boot.jackson.JsonComponent

@JsonComponent
data class ProjectDto(var id: String?,
                      var name: String?,
                      var attributes: Collection<Attribute<*>>?,
                      var requirements: Collection<Requirement<*>>?,
                      var creatorId: String?)