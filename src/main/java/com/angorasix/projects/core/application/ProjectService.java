package com.angorasix.projects.core.application;

import com.angorasix.projects.core.domain.project.Project;
import com.angorasix.projects.core.domain.project.ProjectRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service providing functionality for Projects.
 * 
 * @author rozagerardo
 *
 */
@Service
public class ProjectService {

  private final transient ProjectRepository repository;

  /**
   * Main ProjectHandler constructor.
   * 
   * @param repository the repository
   */
  public ProjectService(final ProjectRepository repository) {
    this.repository = repository;
  }

  /**
   * Method to retrieve a collection of {@link Project}s.
   * 
   * @return {@link Flux} of {@link Project}
   */
  public Flux<Project> findProjects() {
    return repository.findAll();
  }

  /**
   * Method to create a new {@link Project}.
   * 
   * @param newProject {@link Project} to persist
   * @return a {@link Mono} with the persisted {@link Project}
   */
  public Mono<Project> createProject(final Project newProject) {
    return repository.save(newProject);
  }

  /**
   * Method to update an existing {@link Project}.
   * 
   * @param updatedProject the {@link Project} to be updated
   * @return a {@link Mono} with the persisted {@link Project}
   */
  public Mono<Project> updateProject(final Project updatedProject) {
    return repository.save(updatedProject);
  }

  /**
   * Method to find a single {@link Project} from an id.
   * 
   * @param projectId {@link Project} id
   * @return a {@link Mono} with the persisted {@link Project}
   */
  public Mono<Project> findSingleProject(final String projectId) {
    return repository.findById(projectId);
  }

}
