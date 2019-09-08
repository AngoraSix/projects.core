package com.angorasix.projects.core.presentation.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.angorasix.projects.core.application.ProjectService;
import com.angorasix.projects.core.domain.contributor.Contributor;
import com.angorasix.projects.core.domain.project.Project;
import com.angorasix.projects.core.presentation.dto.ProjectDto;
import java.net.URI;
import java.time.ZoneId;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Project Handler (Controller) containing all handler functions related to Project endpoints.
 * 
 * @author rozagerardo
 *
 */
@Component
public class ProjectHandler {

  private final transient ProjectService service;

  /**
   * Main ProjectHandler constructor.
   * 
   * @param service the {@link ProjectService} in the application layer
   */
  public ProjectHandler(final ProjectService service) {
    this.service = service;
  }

  /**
   * Handler for the List Projects endpoint, retrieving a Flux including all persisted Projects.
   * 
   * @param request - HTTP {@code ServerRequest} object
   * @return the {@code ServerResponse}
   */
  public Mono<ServerResponse> listProjects(final ServerRequest request) {
    final Flux<Project> projects = service.findProjects();
    return ok().contentType(MediaType.APPLICATION_JSON).body(projects, Project.class);
  }

  /**
   * Handler for the Create Projects endpoint, to create a new Project entity.
   * 
   * @param request - HTTP {@code ServerRequest} object
   * @return the {@code ServerResponse}
   */
  public Mono<ServerResponse> createProject(final ServerRequest request) {
    // TODO: obtain contributor and Zone info
    final Mono<Project> project = request.bodyToMono(ProjectDto.class)
        .map(projectDto -> new Project(projectDto.getName(), new Contributor("id-test"),
            ZoneId.systemDefault()))
        .flatMap(service::createProject);
    // TODO HATEOAS for location header
    return created(URI.create("http://localhost:8080/gertest"))
        .contentType(MediaType.APPLICATION_JSON)
        .body(project, Project.class);
  }

  /**
   * Handler for the Get Single Project endpoint, retrieving a Mono with the requested Project.
   * 
   * @param request - HTTP {@code ServerRequest} object
   * @return the {@code ServerResponse}
   */
  public Mono<ServerResponse> getProject(final ServerRequest request) {
    final String projectId = request.pathVariable("id");
    final Mono<Project> project = service.findSingleProject(projectId);
    return ok().contentType(MediaType.APPLICATION_JSON)
        .body(project, Project.class)
        .switchIfEmpty(notFound().build());
  }

}
