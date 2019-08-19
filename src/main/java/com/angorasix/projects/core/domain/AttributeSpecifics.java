package com.angorasix.projects.core.domain;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value Object.
 * 
 * <p>Has the attributes to figure out how an attribute can be related to other attributes.
 * 
 * @author rozagerardo
 *
 */
@Getter
@AllArgsConstructor
public class AttributeSpecifics {

  private final Map<String, String> specifics;

}
