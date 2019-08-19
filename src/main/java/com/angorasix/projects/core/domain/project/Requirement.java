package com.angorasix.projects.core.domain.project;

import com.angorasix.projects.core.domain.Attribute;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value Object.
 * 
 * <p>Attribute of something, that conforms a requirement of a Project.
 * 
 * @author rozagerardo
 *
 */
@Getter
@AllArgsConstructor
public final class Requirement<P> {

  private final String article;
  private final Attribute<P> attribute;

}
