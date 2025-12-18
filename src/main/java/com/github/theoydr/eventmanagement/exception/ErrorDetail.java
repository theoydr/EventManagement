package com.github.theoydr.eventmanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
        String key,
        String defaultMessage,
        Map<String, Object> arguments
) {
    // A convenience constructor for errors with no arguments.
    public ErrorDetail(String key, String defaultMessage) {
        this(key, defaultMessage, null);
    }
}
