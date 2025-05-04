package com.angorasix.projects.core.infrastructure.persistence.repository

import com.angorasix.commons.domain.A6Contributor
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
        simpleContributor: A6Contributor?,
    ): Flow<Project>

    suspend fun findForContributorUsingFilter(
        filter: ListProjectsFilter,
        simpleContributor: A6Contributor?,
    ): Project?
}
