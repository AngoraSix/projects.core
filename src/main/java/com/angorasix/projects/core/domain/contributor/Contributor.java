package com.angorasix.projects.core.domain.contributor;

import com.angorasix.projects.core.domain.Attribute;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Root of Contributor aggregate.
 * 
 * <p>A person with a collection of Attributes that can interact with Projects.
 * 
 * @author rozagerardo
 *
 */
@Getter
@Setter
public class Contributor {

  private final String id;
  private List<Attribute<?>> attributes;

  public Contributor(final String id) {
    this.id = id;
  }

}
