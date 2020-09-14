package com.angorasix.projects.core.presentation.dto

data class ProjectDto(
    var id: String?,
    var name: String?,
    var attributes: Collection<AttributeDto>?,
    var requirements: Collection<AttributeDto>?,
    var creatorId: String?
)
