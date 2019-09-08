package com.angorasix.projects.core.presentation.router;

import com.angorasix.projects.core.presentation.dto.ProjectDto;
import com.angorasix.projects.core.presentation.handler.ProjectHandler;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class ProjectRouterUnitTest {

  ProjectRouter router;

  @BeforeEach
  void init(@Mock ProjectHandler handler) {
    router = new ProjectRouter(handler);
  }

  @Test
  public void givenProjectRouter_whenExpectedAPIsRequested_thenRouterRoutesCorrectly()
      throws Exception {
    RouterFunction<ServerResponse> outputRouter = router.projectRouterFunction();

    ServerRequest getAllProjectsRequest =
        MockServerRequest.builder().uri(new URI("/projects")).build();
    ServerRequest getSingleProjectRequest =
        MockServerRequest.builder().uri(new URI("/projects/1")).build();
    ServerRequest getCreateProjectRequest = MockServerRequest.builder().method(HttpMethod.POST)
        .uri(new URI("/projects")).body(new ProjectDto("test-project", null, null));
    ServerRequest invalidRequest =
        MockServerRequest.builder().uri(new URI("/invalid-path")).build();

    StepVerifier.create(outputRouter.route(getAllProjectsRequest)).expectNextCount(1)
        .expectComplete().verify();
    StepVerifier.create(outputRouter.route(getSingleProjectRequest)).expectNextCount(1)
        .expectComplete().verify();
    StepVerifier.create(outputRouter.route(getCreateProjectRequest)).expectNextCount(1)
        .expectComplete().verify();
    StepVerifier.create(outputRouter.route(invalidRequest)).expectNextCount(0).expectComplete()
        .verify();
  }

}
