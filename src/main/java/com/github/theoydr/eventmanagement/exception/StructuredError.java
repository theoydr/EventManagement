package com.github.theoydr.eventmanagement.exception;

import java.util.Map;

public interface StructuredError {

    Map<String, Object> getArguments();
}
