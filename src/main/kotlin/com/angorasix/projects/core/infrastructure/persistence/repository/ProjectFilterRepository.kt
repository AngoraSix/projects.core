package com.angorasix.projects.core.infrastructure.persistence.repository

import com.angorasix.commons.domain.RequestingContributor
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import kotlinx.coroutines.flow.Flow

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface ProjectFilterRepository {

    fun findUsingFilter(
        filter: ListProjectsFilter,
        requestingContributor: RequestingContributor?,
    ): Flow<Project>

    suspend fun findByIdForContributor(
        filter: ListProjectsFilter,
        requestingContributor: RequestingContributor?,
    ): Project?
}
