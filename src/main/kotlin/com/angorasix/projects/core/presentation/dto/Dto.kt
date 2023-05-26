package com.angorasix.projects.core.presentation.dto

import com.angorasix.commons.domain.SimpleContributor
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.hateoas.RepresentationModel
import java.time.ZonedDateTime

/**
 *
 *
 * @author rozagerardo
 */
data class AttributeDto(val key: String, val value: String)

data class ProjectDto(
    var id: String? = null,
    var name: String? = null,
    var attributes: MutableSet<AttributeDto> = mutableSetOf(),
    var requirements: MutableSet<AttributeDto> = mutableSetOf(),
    var creatorId: String? = null,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var private: Boolean? = null,
    var admins: Set<SimpleContributor>? = mutableSetOf(),
    var createdAt: ZonedDateTime? = null,
) : RepresentationModel<ProjectDto>() {
    constructor(
        name: String,
        attributes: MutableSet<AttributeDto> = mutableSetOf(),
    ) : this(null, name, attributes)
}

data class IsAdminDto(val isAdmin: Boolean)
