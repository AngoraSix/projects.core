package com.angorasix.projects.core.domain.project

import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface ProjectRepository : CoroutineSortingRepository<Project, String>
