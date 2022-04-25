package com.angorasix.projects.core.presentation.dto

import java.time.ZonedDateTime

/**
 *
 *
 * @author rozagerardo
 */
data class AttributeDto(
        val key: String,
        val value: String,
)

data class ProjectDto(
        var id: String? = null,
        var name: String? = null,
        var attributes: MutableSet<AttributeDto> = mutableSetOf(),
        var requirements: MutableSet<AttributeDto> = mutableSetOf(),
        var creatorId: String? = null,
        var adminId: String? = null,
        var createdAt: ZonedDateTime? = null
) {

    constructor(
            name: String,
            attributes: MutableSet<AttributeDto> = mutableSetOf<AttributeDto>()
    ) : this(
            null,
            name,
            attributes,
    )
}

data class ContributorHeaderDto(
        var contributorId: String,
        var attributes: Map<String, String> = mutableMapOf(),
)
