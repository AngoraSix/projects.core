package com.angorasix.projects.core.infrastructure.queryfilters

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

/**
 * <p> Classes containing different Request Query Filters.
 * </p>
 *
 * @author rozagerardo
 */
data class ListProjectsFilter(
    val ids: Collection<String>? = null,
    val adminId: Collection<String>? = null,
    val private: Boolean? = null,
) {
    fun toMultiValueMap(): MultiValueMap<String, String> {
        val multiMap: MultiValueMap<String, String> = LinkedMultiValueMap()
        multiMap.add("ids", ids?.joinToString(","))
        multiMap.add("private", private.toString())
        multiMap.add("adminId", adminId?.joinToString(","))
        return multiMap
    }

    companion object {
        fun fromMultiValueMap(multiMap: MultiValueMap<String, String>): ListProjectsFilter {
            return ListProjectsFilter(
                multiMap.getFirst("ids")?.split(","),
                multiMap.getFirst("adminId")?.split(","),
                multiMap.getFirst("private")?.toBoolean(),
            )
        }
    }
}
