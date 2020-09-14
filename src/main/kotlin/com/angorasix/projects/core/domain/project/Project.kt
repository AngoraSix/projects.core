package com.angorasix.projects.core.domain.project

import com.angorasix.projects.core.domain.attribute.Attribute
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional

/**
 * Project Aggregate Root.
 *
 * A Project will contain a series of Attributes that can be used to search (a suitable
 * Contributor, a suitable Project...).
 *
 * @author rozagerardo
 */
data class Project @PersistenceConstructor private constructor(
    @field:Id val id: String?,
    var name: String,
    var attributes: MutableCollection<Attribute<*>> = mutableSetOf<Attribute<*>>(),
    val createdAt: ZonedDateTime,
    var requirements: Collection<Attribute<*>> = mutableSetOf<Attribute<*>>(),
    val creatorId: String
) {

    /**
     * The final constructor that sets all initial fields.
     *
     * @param name - the name of the Project, which will be used to generate the id
     * @param creator - a reference to the `Contributor` that created the `Project`
     * @param zone - the `ZoneId` used to indicate the createdAt timestamp
     */
    constructor(
        name: String,
        creatorId: String,
        zone: ZoneId?
    ) : this(
        null,
        name,
        mutableSetOf<Attribute<*>>(),
        ZonedDateTime.now(
            Optional.ofNullable<ZoneId>(zone)
                .orElse(ZoneId.systemDefault())
        ),
        emptySet(),
        creatorId
    )

    /**
     * Add a single attribute to the list.
     *
     * @param attribute - attribute to be added to the list
     */
    fun addAttribute(attribute: Attribute<*>) {
        attributes.add(attribute)
    }
}
