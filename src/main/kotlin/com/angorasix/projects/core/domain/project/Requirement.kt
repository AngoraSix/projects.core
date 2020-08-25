package com.angorasix.projects.core.domain.project

import com.angorasix.projects.core.domain.Attribute

/**
 * Value Object.
 *
 * Attribute of something, that conforms a requirement of a Project.
 *
 * @author rozagerardo
 */
data class Requirement<P>(val article: String?,
                          val attribute: Attribute<P>?) {}