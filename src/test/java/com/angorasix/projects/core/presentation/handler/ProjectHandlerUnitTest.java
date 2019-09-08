package com.angorasix.projects.core.presentation.handler;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.angorasix.projects.core.application.ProjectService;
import com.angorasix.projects.core.domain.contributor.Contributor;
import com.angorasix.projects.core.domain.project.Project;
import com.angorasix.projects.core.presentation.dto.ProjectDto;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.EntityResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class ProjectHandlerUnitTest {

  ProjectHandler handler;

  @Mock
  ProjectService service;

  @BeforeEach
  void init() {
    handler = new ProjectHandler(service);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void givenExistingProjects_whenListProjects_thenHandlerRetrievesOkResposeWithFlux()
      throws Exception {
    ServerRequest mockedRequest = MockServerRequest.builder().build();
    Project mockedProject =
        new Project("mockedProjectName", new Contributor("id-test"), ZoneId.systemDefault());
    Flux<Project> retrievedProject = Flux.just(mockedProject);
    when(service.findProjects()).thenReturn(retrievedProject);

    Mono<ServerResponse> outputResponse = handler.listProjects(mockedRequest);

    StepVerifier.create(outputResponse).assertNext(response -> {
      assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
      assertThat(((EntityResponse<Flux<Project>>) response).entity()).isSameAs(retrievedProject);
    }).expectComplete().verify();
    verify(service).findProjects();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void givenExistingProjects_whenCreateProject_thenHandlerRetrievesOkResposeWithMono()
      throws Exception {

    ProjectDto mockedProjectDto = new ProjectDto("mockedProjectName", emptyList(), emptyList());
    ServerRequest mockedRequest = MockServerRequest.builder().body(Mono.just(mockedProjectDto));
    Project mockedProject =
        new Project("mockedProjectName", new Contributor("id-test"), ZoneId.systemDefault());
    when(service.createProject(any(Project.class))).thenReturn(Mono.just(mockedProject));

    Mono<ServerResponse> outputResponse = handler.createProject(mockedRequest);

    StepVerifier.create(outputResponse).assertNext(response -> {
      assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED);
      StepVerifier.create(((EntityResponse<Mono<Project>>) response).entity())
          .assertNext(project -> {
            assertThat(project.getName()).isEqualTo("mockedProjectName");
            assertThat(project.getCreator().getId()).isEqualTo("id-test");
          })
          .expectComplete()
          .verify();
    }).expectComplete().verify();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void givenExistingProjects_whenGetProject_thenHandlerRetrievesOkResposeWithMono()
      throws Exception {
    String projectId = "projectId";
    ServerRequest mockedRequest = MockServerRequest.builder().pathVariable("id", projectId).build();
    Mono<Project> retrievedProject = Mono
        .just(new Project("mockedProjectName", new Contributor("id-test"), ZoneId.systemDefault()));
    when(service.findSingleProject(projectId)).thenReturn(retrievedProject);

    Mono<ServerResponse> outputResponse = handler.getProject(mockedRequest);

    StepVerifier.create(outputResponse).assertNext(response -> {
      assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
      assertThat(((EntityResponse<Mono<Project>>) response).entity()).isSameAs(retrievedProject);
    }).expectComplete().verify();
    verify(service).findSingleProject(projectId);
  }

}
