package com.angorasix.projects.core.domain.project

import com.angorasix.projects.core.infrastructure.persistence.repository.ProjectFilterRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface ProjectRepository : CoroutineSortingRepository<Project, String>, ProjectFilterRepository


