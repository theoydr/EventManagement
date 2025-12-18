package com.github.theoydr.eventmanagement.exception;

import java.util.Map;

/**
 * Exception thrown when a requested resource is not found.
 * This class now holds structured data about the resource for richer error handling.
 */
public class ResourceNotFoundException extends RuntimeException implements StructuredError {

    private final String resourceType;
    private final String identifierType;
    private final Object identifierValue;

    public ResourceNotFoundException(String resourceType, String identifierType, Object identifierValue) {
        // The message is still generated for logging and fallback purposes.
        super(String.format("%s not found with %s: '%s'", resourceType, identifierType, identifierValue));
        this.resourceType = resourceType;
        this.identifierType = identifierType;
        this.identifierValue = identifierValue;
    }

    /**
     * Returns the structured error arguments for building a detailed API error response.
     * @return A map of the error details.
     */
    @Override
    public Map<String, Object> getArguments() {
        return Map.of(
                "resourceType", resourceType,
                "identifierType", identifierType,
                "identifierValue", identifierValue
        );
    }

    public String getResourceType() {
        return resourceType;
    }

    public Object getIdentifierValue() {
        return identifierValue;
    }

    public String getIdentifierType() {
        return identifierType;
    }
}

