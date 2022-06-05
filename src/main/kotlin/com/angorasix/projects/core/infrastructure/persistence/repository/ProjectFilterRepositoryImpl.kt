package com.angorasix.projects.core.infrastructure.persistence.repository

import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query


/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ProjectFilterRepositoryImpl(val mongoOps: ReactiveMongoOperations) : ProjectFilterRepository {

    override fun findUsingFilter(filter: ListProjectsFilter): Flow<Project> {
        return mongoOps.find(filter.toQuery(), Project::class.java).asFlow()
    }
}

private fun ListProjectsFilter.toQuery(): Query {
    val query = Query()
    ids?.let { query.addCriteria(where("_id").`in`(it)) }
    adminId?.let { query.addCriteria(where("adminId").`is`(it)) }
    return query
}