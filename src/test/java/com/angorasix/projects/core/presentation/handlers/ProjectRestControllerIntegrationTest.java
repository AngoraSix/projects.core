package com.angorasix.projects.core.presentation.handlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import com.angorasix.projects.core.presentation.handlers.ProjectHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectHandler.class)
public class ProjectRestControllerIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Test
  public void cicd_test() throws Exception {
    mockMvc.perform(get("/projects")).andExpect(content().string("TODO - projects"));
  }

}
