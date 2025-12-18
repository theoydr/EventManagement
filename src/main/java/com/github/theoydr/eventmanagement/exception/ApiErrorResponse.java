package com.github.theoydr.eventmanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        HttpStatus status,
        String message,
        ErrorDetail error,
        Map<String, ErrorDetail> fieldErrors
) {

    /**
     * Static factory method to create an ApiErrorResponse for a single, general error.
     * @param status The HTTP status.
     * @param message The high-level summary message.
     * @param error The structured error detail.
     * @return A new ApiErrorResponse instance.
     */
    public static ApiErrorResponse forGeneralError(HttpStatus status, String message, ErrorDetail error) {
        return new ApiErrorResponse(status, message, error, null);
    }

    /**
     * Static factory method to create an ApiErrorResponse for validation failures.
     * @param status The HTTP status.
     * @param message The high-level summary message.
     * @param fieldErrors A map of field-specific errors.
     * @return A new ApiErrorResponse instance.
     */
    public static ApiErrorResponse forValidationError(HttpStatus status, String message, Map<String, ErrorDetail> fieldErrors) {
        return new ApiErrorResponse(status, message, null, fieldErrors);
    }
}

