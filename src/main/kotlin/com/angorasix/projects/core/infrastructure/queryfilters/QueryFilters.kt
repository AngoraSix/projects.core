package com.angorasix.projects.core.infrastructure.queryfilters

/**
 * <p> Classes containing different Request Query Filters.
 * </p>
 *
 * @author rozagerardo
 */
data class ListProjectsFilter(
        val ids: Collection<String>? = null,
        val adminId: String? = null
)