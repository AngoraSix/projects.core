package com.angorasix.projects.core.presentation.dto

import java.time.ZonedDateTime

data class ProjectDto(
    var id: String?,
    var name: String?,
    var attributes: Collection<AttributeDto>?,
    var requirements: Collection<AttributeDto>?,
    var creatorId: String?,
    var createdAt: ZonedDateTime?
) {

    constructor(
        name: String,
        attributes: Collection<AttributeDto> = mutableSetOf<AttributeDto>()
    ) : this(
        null,
        name,
        attributes,
        emptyList(),
        null,
        null
    )
}
