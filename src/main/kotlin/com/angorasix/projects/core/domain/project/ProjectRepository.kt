package com.angorasix.projects.core.domain.project

import com.angorasix.projects.core.infrastructure.persistence.repository.ProjectFilterRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface ProjectRepository :
    CoroutineSortingRepository<Project, String>,
    ProjectFilterRepository,
    CoroutineCrudRepository<Project, String>
