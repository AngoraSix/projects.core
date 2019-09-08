package com.angorasix.projects.core.presentation.router;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.angorasix.projects.core.presentation.handler.ProjectHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Router for all Project related endpoints.
 * 
 * @author rozagerardo
 *
 */
@Configuration
public class ProjectRouter {

  private final transient ProjectHandler handler;

  /**
   * Main ProjectRouter builder.
   * 
   * @param handler {@link ProjectHandler} that provides the functionality
   */
  public ProjectRouter(final ProjectHandler handler) {
    this.handler = handler;
  }

  /**
   * Main RouterFunction configuration for all endpoints related to Projects.
   * 
   * @return the {@link RouterFunction} with all the routes for Projects
   */
  @Bean
  public RouterFunction<ServerResponse> projectRouterFunction() {
    return route()
        .path("/projects",
            builder -> builder.GET("", handler::listProjects)
                .GET("/{id}", handler::getProject)
                .POST("", handler::createProject))
        .build();
  }

}
