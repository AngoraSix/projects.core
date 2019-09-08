package com.angorasix.projects.core.presentation.dto;

import com.angorasix.projects.core.domain.Attribute;
import com.angorasix.projects.core.domain.project.Requirement;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectDto {

  private String name;
  private Collection<Attribute<?>> attributes;
  private Collection<Requirement<?>> requirements;

}
