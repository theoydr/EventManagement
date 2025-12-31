package com.github.theoydr.eventmanagement.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Exception thrown when a user attempts an operation they are not authorized to perform
 * based on their current role or status.
 */
public class OperationNotAllowedException extends RuntimeException {

    public OperationNotAllowedException(String reason) {
      super(reason);
    }


  public Map<String, Object> getArguments() {
    return Collections.emptyMap();
  }
}
