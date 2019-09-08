package com.angorasix.projects.core.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.angorasix.projects.core.domain.contributor.Contributor;
import com.angorasix.projects.core.domain.project.Project;
import com.angorasix.projects.core.domain.project.ProjectRepository;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceUnitTest {

  ProjectService service;

  @Mock
  ProjectRepository repository;

  @BeforeEach
  void init() {
    service = new ProjectService(repository);
  }

  @Test
  public void givenExistingProjects_whenFindProjects_thenServiceRetrievesFluxOfProjects()
      throws Exception {
    Project mockedProject =
        new Project("mockedProjectName", new Contributor("creator_id"), ZoneId.systemDefault());
    when(repository.findAll()).thenReturn(Flux.just(mockedProject));

    Flux<Project> outputProjects = service.findProjects();

    StepVerifier.create(outputProjects).assertNext(project -> {
      assertThat(project).isSameAs(mockedProject);
    }).expectComplete().verify();
    verify(repository).findAll();
  }

  @Test
  public void givenExistingProject_whenFindSingleProjects_thenServiceRetrievesMonoWithProject()
      throws Exception {
    String mockedProjectId = "id1";
    Project mockedProject =
        new Project("mockedProjectName", new Contributor("creator_id"), ZoneId.systemDefault());
    when(repository.findById(mockedProjectId)).thenReturn(Mono.just(mockedProject));

    Mono<Project> outputProject = service.findSingleProject(mockedProjectId);

    StepVerifier.create(outputProject).assertNext(project -> {
      assertThat(project).isSameAs(mockedProject);
    }).expectComplete().verify();
    verify(repository).findById(mockedProjectId);
  }

  @Test
  public void whenCreateProject_thenServiceRetrieveSavedProject() throws Exception {
    Project mockedProject =
        new Project("mockedProjectName", new Contributor("creator_id"), ZoneId.systemDefault());
    Project savedProject =
        new Project("savedProjectName", new Contributor("creator_id"), ZoneId.systemDefault());
    when(repository.save(mockedProject)).thenReturn(Mono.just(savedProject));

    Mono<Project> outputProject = service.createProject(mockedProject);

    StepVerifier.create(outputProject).assertNext(project -> {
      assertThat(project).isSameAs(savedProject);
    }).expectComplete().verify();
    verify(repository).save(mockedProject);
  }

  @Test
  public void whenUpdateProject_thenServiceRetrieveUpdatedProject() throws Exception {
    Project mockedProject =
        new Project("mockedProjectName", new Contributor("creator_id"), ZoneId.systemDefault());
    Project updatedProject =
        new Project("updatedProjectName", new Contributor("creator_id"), ZoneId.systemDefault());;
    when(repository.save(mockedProject)).thenReturn(Mono.just(updatedProject));

    Mono<Project> outputProject = service.createProject(mockedProject);

    StepVerifier.create(outputProject).assertNext(project -> {
      assertThat(project).isSameAs(updatedProject);
    }).expectComplete().verify();
    verify(repository).save(mockedProject);
  }

}
