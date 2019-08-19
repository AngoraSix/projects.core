package com.angorasix.projects.core.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value Object.
 * 
 * <p>Parametrized attribute that can be compared
 * 
 * @author rozagerardo
 *
 */
@Getter
@AllArgsConstructor
public final class Attribute<P> {

  private final P value;
  private final List<Attribute<?>> refs;
  private final AttributeSpecifics specifics;

}
