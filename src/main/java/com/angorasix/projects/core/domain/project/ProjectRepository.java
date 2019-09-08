package com.angorasix.projects.core.domain.project;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;

public interface ProjectRepository extends ReactiveSortingRepository<Project, String> {

}
