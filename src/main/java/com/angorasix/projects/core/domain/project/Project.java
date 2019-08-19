package com.angorasix.projects.core.domain.project;

import com.angorasix.projects.core.domain.Attribute;
import com.angorasix.projects.core.domain.contributor.Contributor;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Project Aggregate Root.
 * 
 * <p>A Project will contain a series of Attributes that can be used to search (a suitable
 * Contributor, a suitable Project...).
 * 
 * @author rozagerardo
 *
 */
@Getter
@Setter
public class Project {

  private final String id;
  private List<Attribute<?>> attributes;
  private ZonedDateTime createdAt;
  private String description;
  private List<Requirement<?>> requirements;
  private Contributor admin;

  protected Project(final String id) {
    this.id = id;
  }

}
