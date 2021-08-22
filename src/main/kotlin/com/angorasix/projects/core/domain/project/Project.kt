package com.angorasix.projects.core.domain.project

import com.angorasix.projects.core.domain.attribute.Attribute
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor
import java.time.ZoneId
import java.time.ZonedDateTime

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
    val creatorId: String,
    val createdAt: ZonedDateTime,
    var attributes: MutableSet<Attribute<*>> = mutableSetOf<Attribute<*>>(),
    var requirements: MutableSet<Attribute<*>> = mutableSetOf<Attribute<*>>(),
) {

    /**
     * The final constructor that sets all initial fields.
     *
     * @param name - the name of the Project, which will be used to generate the id
     * @param creatorId - a reference to the `Contributor` that created the `Project`
     * @param zone - the `ZoneId` used to indicate the createdAt timestamp
     * @param attributes - a set of initial attributes
     */
    constructor(
        name: String,
        creatorId: String,
        zone: ZoneId? = ZoneId.systemDefault(),
        attributes: MutableSet<Attribute<*>> = mutableSetOf(),
    ) : this(
        null,
        name,
        creatorId,
        ZonedDateTime.now(zone),
        attributes,
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
