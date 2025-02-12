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

    override suspend fun findForContributorUsingFilter(
        filter: ListProjectsFilter,
        simpleContributor: SimpleContributor?,
    ): Project? {
        return mongoOps.find(filter.toQuery(simpleContributor), Project::class.java)
            .awaitFirstOrNull()
    }
}

private fun ListProjectsFilter.toQuery(simpleContributor: SimpleContributor?): Query {
    val query = Query()
    val requestingOthers = adminId == null || !adminId.contains(simpleContributor?.contributorId)
    val requestingOwn =
        simpleContributor != null && (adminId.isNullOrEmpty() || adminId.contains(simpleContributor.contributorId))
    val orCriteria = mutableListOf<Criteria>()

    if (requestingOthers) {
        if (private == true && !requestingOwn) {
            query.addCriteria(where("_id").`is`(null))
            return query
        }
        orCriteria.add(
            Criteria().andOperator(
                adminId?.let {
                    where("admins").elemMatch(where("contributorId").`in`(it as Collection<Any>))
                } ?: where("admins").not()
                    .elemMatch(where("contributorId").`is`(simpleContributor?.contributorId)),
                where("private").`is`(false),
            ),
        )
    }

    if (requestingOwn) {
        orCriteria.add(
            private?.let {
                Criteria().andOperator(
                    where("admins").elemMatch(where("contributorId").`is`(simpleContributor?.contributorId)),
                    where("private").`is`(it),
                )
            }
                ?: where("admins").elemMatch(where("contributorId").`is`(simpleContributor?.contributorId)),
        )
    }

    query.addCriteria(Criteria().orOperator(orCriteria))

    ids?.let { query.addCriteria(where("_id").`in`(it as Collection<Any>)) }
    return query
}
