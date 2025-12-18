package com.github.theoydr.eventmanagement.controller;

import com.github.theoydr.eventmanagement.constants.MessageKeys;
import com.github.theoydr.eventmanagement.exception.ApiErrorResponse;
import com.github.theoydr.eventmanagement.exception.ErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;

import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;


/**
 * A custom controller to handle errors that occur outside the MVC layer
 * (e.g., Filter exceptions, 404s for non-existent endpoints).
 * This replaces Spring Boot's default BasicErrorController ("Whitelabel Error Page")
 * to ensure that ALL errors return our standardized ApiErrorResponse structure.
 */
@RestController
public class CustomErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);
    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * Catches all errors forwarded to /error.
     */
    @RequestMapping("/error")
    public ResponseEntity<ApiErrorResponse> handleError(HttpServletRequest request) {
        try {
            WebRequest webRequest = new ServletWebRequest(request);
            Map<String, Object> attributes = getErrorAttributes(webRequest);

            HttpStatus status = getHttpStatus(attributes);
            String key = resolveErrorKey(status);
            String message = resolveErrorMessage(status, attributes);

            logError(webRequest, status, attributes, message);

            Map<String, Object> args = getErrorArguments(attributes);
            ErrorDetail errorDetail = new ErrorDetail(key, message, args);

            return new ResponseEntity<>(
                    ApiErrorResponse.forGeneralError(status, "Request Failed", errorDetail),
                    status
            );

        } catch (Exception e) {
            return handleSafetyNetError(e);
        }
    }

    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        // We use Include.MESSAGE to get the exception message, but exclude EXCEPTION to prevent
        // leaking internal class names, which is a security risk (Information Disclosure).
        return errorAttributes.getErrorAttributes(
                webRequest,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
        );
    }

    private HttpStatus getHttpStatus(Map<String, Object> attributes) {
        Integer statusCode = (Integer) attributes.get("status");
        return HttpStatus.valueOf(statusCode != null ? statusCode : 500);
    }

    private String resolveErrorKey(HttpStatus status) {
        return (status == HttpStatus.NOT_FOUND) ? MessageKeys.Error.ENDPOINT_NOT_FOUND : MessageKeys.Error.UNEXPECTED;
    }

    private String resolveErrorMessage(HttpStatus status, Map<String, Object> attributes) {
        if (status == HttpStatus.NOT_FOUND) {
            return "The requested endpoint was not found.";
        }

        if (status.is5xxServerError()) {
            return "An unexpected internal error occurred.";
        }

        // For 4xx Client Errors (e.g. Bad Request from a Filter), trust the message
        String message = (String) attributes.get("message");
        return (message == null || message.isEmpty()) ? "Request Failed" : message;
    }

    private Map<String, Object> getErrorArguments(Map<String, Object> attributes) {
        String path = (String) attributes.get("path");
        return path != null ? Map.of("path", path) : null;
    }

    private void logError(WebRequest webRequest, HttpStatus status, Map<String, Object> attributes, String message) {
        if (status == HttpStatus.NOT_FOUND) {
            // Log 404s as WARN to avoid noise
            log.warn("404 Not Found: {}", attributes.get("path"));
        } else {
            // For 500s or other errors, retrieve the original exception for full context
            Throwable originalError = errorAttributes.getError(webRequest);
            log.error("An unexpected error occurred (outside MVC layer): Path={}, Message={}",
                    attributes.get("path"), message, originalError);
        }
    }

    private ResponseEntity<ApiErrorResponse> handleSafetyNetError(Exception e) {
        log.error("CRITICAL: CustomErrorController failed to process the error response.", e);
        return new ResponseEntity<>(
                ApiErrorResponse.forGeneralError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Critical system failure during error handling",
                        null
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}