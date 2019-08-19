package com.angorasix.projects.core.presentation.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import com.angorasix.projects.core.presentation.handlers.ProjectHandler;
import org.junit.jupiter.api.Test;

public class ProjectRestControllerUnitTest {

  @Test
  public void coverageTest() {
    ProjectHandler controller = new ProjectHandler();

    String output = controller.getAllProjects();

    assertThat(output).isEqualTo("TODO - projects");
  }

}
