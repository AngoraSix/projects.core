package com.angorasix.projects.core.infrastructure.persistence.repository

import com.angorasix.commons.domain.SimpleContributor
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
        simpleContributor: SimpleContributor?,
    ): Flow<Project> {
        return mongoOps.find(filter.toQuery(simpleContributor), Project::class.java).asFlow()
    }

    override suspend fun findByIdForContributor(
        filter: ListProjectsFilter,
        simpleContributor: SimpleContributor?,
    ): Project? {
        return mongoOps.find(filter.toQuery(simpleContributor), Project::class.java)
            .awaitFirstOrNull()
    }
}

private fun ListProjectsFilter.toQuery(simpleContributor: SimpleContributor?): Query {
    val query = Query()
    val requestingOthers = adminId == null || adminId != simpleContributor?.id
    val requestingOwn =
        simpleContributor != null && (adminId == null || adminId == simpleContributor.id)

    if (requestingOthers) {
        if (private == true && !requestingOwn) {
            query.addCriteria(where("_id").`is`(null))
            return query
        }
        val othersCriteria = Criteria().andOperator(
            adminId?.let { where("adminId").`is`(it) }
                ?: where("adminId").ne(simpleContributor?.id),
            where("private").`is`(false),
        )
        query.addCriteria(othersCriteria)
    }

    if (requestingOwn) {
        val ownCriteria = private?.let {
            Criteria().andOperator(
                where("adminId").`is`(simpleContributor?.id),
                where("private").`is`(it),
            )
        } ?: where("adminId").`is`(simpleContributor?.id)
        query.addCriteria(ownCriteria)
    }

    ids?.let { query.addCriteria(where("_id").`in`(it)) }
    return query
}
