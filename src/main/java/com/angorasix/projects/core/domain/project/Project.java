package com.angorasix.projects.core.domain.project;

import static java.util.Collections.emptySet;

import com.angorasix.projects.core.domain.Attribute;
import com.angorasix.projects.core.domain.contributor.Contributor;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

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
public class Project {

  @Id
  private String id;
  @Setter
  private String name;
  @Setter
  private Collection<Attribute<?>> attributes;
  private final ZonedDateTime createdAt;
  @Setter
  private Collection<Requirement<?>> requirements;
  private final Contributor creator;

  /**
   * The final constructor that sets all initial fields.
   * 
   * @param name - the name of the Project, which will be used to generate the id
   * @param creator - a reference to the {@code Contributor} that created the {@code Project}
   * @param zone - the {@code ZoneId} used to indicate the createdAt timestamp
   */
  public Project(final String name, final Contributor creator, final ZoneId zone) {
    this(null, name, emptySet(), ZonedDateTime.now(zone), emptySet(), creator);
  }

  @PersistenceConstructor
  private Project(String id, String name, Collection<Attribute<?>> attributes,
      ZonedDateTime createdAt, Collection<Requirement<?>> requirements, Contributor creator) {
    super();
    this.id = id;
    this.name = name;
    this.attributes = attributes;
    this.createdAt = createdAt;
    this.requirements = requirements;
    this.creator = creator;
  }



  /**
   * Add a single attribute to the list.
   * 
   * @param attribute - attribute to be added to the list
   */
  public void addAttribute(final Attribute<?> attribute) {
    this.attributes.add(attribute);
  }

}
