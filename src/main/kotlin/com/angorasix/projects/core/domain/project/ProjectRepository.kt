package com.angorasix.projects.core.domain.project

import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository

interface ProjectRepository : CoroutineSortingRepository<Project, String>