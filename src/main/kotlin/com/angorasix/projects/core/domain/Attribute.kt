package com.angorasix.projects.core.domain

/**
 * Value Object.
 *
 * Parametrized attribute that can be compared
 *
 * @author rozagerardo
 */
data class Attribute<P>(val value: P?,
                        val refs: List<Attribute<*>>?,
                        val specifics: AttributeSpecifics?) {}