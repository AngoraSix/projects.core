package com.angorasix.projects.core.presentation.dto

import com.angorasix.commons.domain.A6Contributor
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.hateoas.RepresentationModel
import java.time.Instant

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
    val id: String? = null,
    val name: String? = null,
    val attributes: MutableSet<AttributeDto> = mutableSetOf(),
    val requirements: MutableSet<AttributeDto> = mutableSetOf(),
    val creatorId: String? = null,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val private: Boolean? = null,
    val admins: Set<A6Contributor>? = mutableSetOf(),
    val createdInstant: Instant? = null,
) : RepresentationModel<ProjectDto>() {
    constructor(
        name: String,
        attributes: MutableSet<AttributeDto> = mutableSetOf(),
    ) : this(null, name, attributes)
}
