package com.angorasix.projects.core.infrastructure.persistence.repository

import com.angorasix.commons.domain.RequestingContributor
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ProjectFilterRepositoryImpl(val mongoOps: ReactiveMongoOperations) : ProjectFilterRepository {

    override fun findUsingFilter(
        filter: ListProjectsFilter,
        requestingContributor: RequestingContributor?,
    ): Flow<Project> {
        return mongoOps.find(filter.toQuery(requestingContributor), Project::class.java).asFlow()
    }

    override suspend fun findByIdForContributor(
        filter: ListProjectsFilter,
        requestingContributor: RequestingContributor?,
    ): Project? {
        return mongoOps.find(filter.toQuery(requestingContributor), Project::class.java)
            .awaitFirstOrNull()
    }
}

private fun ListProjectsFilter.toQuery(requestingContributor: RequestingContributor?): Query {
    val query = Query()

    val requestingOthers = adminId == null || adminId != requestingContributor?.id
    val requestingOwn =
        requestingContributor != null && (adminId == null || adminId == requestingContributor.id)

    if (requestingOthers) {
        if (private == true && !requestingOwn) {
            // we won't be retrieving any project in this case
            query.addCriteria(where("_id").`is`(null))
            return query
        }
        query.addCriteria(
            Criteria().andOperator(
                adminId?.let { where("adminId").`is`(adminId) } ?: where("adminId").ne(requestingContributor?.id),
                where("private").`is`(false),
            ),
        )
    }
    if (requestingOwn) {
        query.addCriteria(
            private?.let {
                Criteria().andOperator(
                    where("adminId").`is`(requestingContributor?.id),
                    where("private").`is`(private),
                )
            } ?: where("adminId").`is`(requestingContributor?.id),
        )
    }
    ids?.let { query.addCriteria(where("_id").`in`(it)) }
    return query
}


