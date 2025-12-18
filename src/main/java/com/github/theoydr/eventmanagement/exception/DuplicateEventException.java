package com.github.theoydr.eventmanagement.exception;

import java.util.Collections;
import java.util.Map;

public class DuplicateEventException extends RuntimeException implements StructuredError {
  public DuplicateEventException() {
    super("This event has already been created by the organizer at the specified time and location.");
  }


  @Override
  public Map<String, Object> getArguments() {
    return Collections.emptyMap();
  }
}

