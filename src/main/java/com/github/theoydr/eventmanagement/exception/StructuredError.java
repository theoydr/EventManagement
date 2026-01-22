package com.github.theoydr.eventmanagement.exception;

import java.util.Map;

public interface StructuredError {

    /**
     * Returns the structured error arguments for building a detailed API error response.
     * @return A map of the error details. If there are no arguments return an empty Map.
     */
    Map<String, Object> getArguments();
}
