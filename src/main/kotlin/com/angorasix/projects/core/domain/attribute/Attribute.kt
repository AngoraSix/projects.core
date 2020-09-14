package com.angorasix.projects.core.domain.attribute

/**
 * Value Object.
 *
 * Parametrized attribute that can be compared
 *
 * @author rozagerardo
 */
data class Attribute<P>(
    val value: P, val key: String
)
