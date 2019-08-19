package com.angorasix.projects.core.presentation.handlers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RestController containing Project endpoints.
 * 
 * @author rozagerardo
 *
 */
@RestController
@RequestMapping("projects")
public class ProjectHandler {

  /**
   * Get all projects - test.
   * 
   * @return hardcoded string just for test
   */
  @GetMapping
  public String getAllProjects() {
    return "TODO - projects";
  }

}
