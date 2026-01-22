package com.github.theoydr.eventmanagement.exception;

import com.github.theoydr.eventmanagement.constants.MessageKeys;
import jakarta.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.DatabindException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A global exception handler for the application.
 * This class uses @RestControllerAdvice to intercept exceptions thrown from any controller
 * and converts them into a standardized ApiErrorResponse.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");
    private static final String GENERIC_FALLBACK_MESSAGE = "Fallback Message";


    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }




    /**
     * Handles validation exceptions triggered by @Valid.
     *
     * @param ex The MethodArgumentNotValidException that was thrown.
     * @return A ResponseEntity containing a structured error response with field-specific messages.
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        try {
            log.warn("Validation failed: {}", ex.getMessage());

            Map<String, ErrorDetail> fieldErrors = ex.getBindingResult().getAllErrors().stream()
                    .collect(Collectors.toMap(
                            error -> (error instanceof FieldError fieldError) ? fieldError.getField() : error.getObjectName(),
                            this::createErrorDetailFromValidationError
                    ));

            ApiErrorResponse body = ApiErrorResponse.forValidationError(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
            return ResponseEntity.badRequest().body(body);
        } catch (Exception internalEx) {

            ApiErrorResponse body =  buildInternalServerErrorResponse(ex, "An unexpected error occurred while processing validation exceptions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);

        }
    }




    private ErrorDetail createErrorDetailFromValidationError(ObjectError error) {

        try {
            ConstraintViolation<?> constraintViolation = error.unwrap(ConstraintViolation.class);

            String key = constraintViolation.getMessageTemplate().replaceAll("[{}]", "");


            Map<String, Object> constraintArguments = extractConstraintArguments(constraintViolation);


            String defaultMessage = getDefaultMessage(constraintArguments, key);

            return new ErrorDetail(key, defaultMessage, constraintArguments.isEmpty() ? null : constraintArguments);
        } catch (Exception ex) {
            log.error("Failed to create ErrorDetail for validation error '{}'. Returning generic fallback.", error.getObjectName(), ex);

            // Fails gracefully: only this field gets a generic error
            return new ErrorDetail(
                    MessageKeys.Error.UNEXPECTED.replaceAll("[{}]", ""),
                    "Unexpected error processing this validation field",
                    Map.of("fieldName", error.getObjectName())
            );
        }

    }

    private Map<String, Object> extractConstraintArguments(ConstraintViolation<?> constraintViolation) {
        Set<String> standardAttributes = Set.of("message", "groups", "payload");


        Map<String, Object> arguments = constraintViolation.getConstraintDescriptor().getAttributes();
        return arguments.entrySet().stream()
                .filter(entry -> !standardAttributes.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtException(Exception ex) {
        ApiErrorResponse body = buildInternalServerErrorResponse(ex, "An unexpected exception occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }





    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        Map<String, Object> args = Map.of("path", "/" + ex.getResourcePath());
        return buildGeneralErrorResponse(args, MessageKeys.Error.ENDPOINT_NOT_FOUND, HttpStatus.NOT_FOUND, "The requested endpoint was not found.");
    }

    /**
     * Handles the case where a requested resource is not found.
     *
     * @param ex The ResourceNotFoundException that was thrown.
     * @return A ResponseEntity with a 404 Not Found status and a clear error message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildGeneralErrorResponse(ex.getArguments(), MessageKeys.Error.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Resource not found");

    }

    /**
     * Handles the case where a user tries to register with an email that already exists.
     *
     * @param ex The UserAlreadyExistsException that was thrown.
     * @return A ResponseEntity with a 409 Conflict status and a clear error message.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return buildGeneralErrorResponse(ex.getArguments(), MessageKeys.Error.USER_EXISTS, HttpStatus.CONFLICT, "User already exists");

    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("JSON parsing failed: {}", ex.getMessage());
        Map<String, Object> args = new HashMap<>();

        if (ex.getCause() instanceof DatabindException dte && dte.getPath() != null) {
            String fieldName = dte.getPath().stream()
                    .map(DatabindException.Reference::getPropertyName)
                    .collect(Collectors.joining("."));

            if (!fieldName.isEmpty()) {
                args.put("field", fieldName);
            }

        }
        return buildGeneralErrorResponse(args, MessageKeys.Error.INVALID_FORMAT, HttpStatus.BAD_REQUEST, "Malformed JSON request or invalid data format.");
    }

    /**
     * Handles business logic errors related to event bookings (e.g., booking a full event).
     *
     * @param ex The EventBookingException that was thrown.
     * @return A ResponseEntity with a 409 Conflict status and a clear error message.
     */
    @ExceptionHandler(EventBookingException.class)
    public ResponseEntity<ApiErrorResponse> handleEventBookingException(EventBookingException ex) {
        return buildGeneralErrorResponse(ex.getArguments(), MessageKeys.Error.BOOKING_FAILED, HttpStatus.CONFLICT, "Booking failed");

    }

    @ExceptionHandler(DuplicateEventException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEventException(DuplicateEventException ex) {
        return buildGeneralErrorResponse(ex.getArguments(), MessageKeys.Error.EVENT_DUPLICATE, HttpStatus.CONFLICT, "Duplicate event detected");

    }



    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleOperationNotAllowedException(OperationNotAllowedException ex) {
        return buildGeneralErrorResponse(ex.getArguments(), MessageKeys.Error.OPERATION_NOT_ALLOWED, HttpStatus.FORBIDDEN, "Operation not allowed");
    }


    /**
     * A centralized helper method to build error responses for general business exceptions.
     */
    private ResponseEntity<ApiErrorResponse> buildGeneralErrorResponse(Map<String, Object> arguments, String templateKey, HttpStatus status, String message) {




        String key = templateKey.replaceAll("[{}]", "");
        log.warn("Business exception occurred. Status: {}, Key: {}, Message: {}, Arguments: {}", status, key, message, arguments);

        try {


            String defaultMessage = getDefaultMessage(arguments, key);


            Map<String, Object> finalArgs = (arguments == null || arguments.isEmpty()) ? null : arguments;
            ErrorDetail errorDetail = new ErrorDetail(key, defaultMessage, finalArgs);
            ApiErrorResponse body = ApiErrorResponse.forGeneralError(status, message, errorDetail);
            return ResponseEntity.status(status).body(body);
        } catch (Exception ex) {
            ApiErrorResponse body = buildInternalServerErrorResponse(ex, "Failed to build general error response due to an internal error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    private String getDefaultMessage(Map<String, Object> arguments, String key) {
        String defaultMessageTemplate = messageSource.getMessage(key, null, "Fallback Message", Locale.ROOT);
        if (GENERIC_FALLBACK_MESSAGE.equals(defaultMessageTemplate)) {

            log.error("Missing message key '{}' in message source. Using fallback.", key);
            return GENERIC_FALLBACK_MESSAGE;
        }

        try {
            return interpolateMessage(defaultMessageTemplate, arguments, key);
        } catch (Exception ex) {
            log.error("Failed to interpolate message for key '{}' in message source. Using fallback.", key, ex);
            return GENERIC_FALLBACK_MESSAGE;
        }
    }


    /**
     * Manually interpolates a message template with named arguments.
     */
    private String interpolateMessage(String template, Map<String, Object> arguments, String originalKey) {
        String result = template;
        if (arguments == null) return result;
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(result);
        if (matcher.find()) {
            // This is a developer error: a mismatch between the message template and its arguments.
            String unreplacedPlaceholder = matcher.group(1);
            throw new IllegalStateException("Failed to interpolate message template for key: '" + originalKey + "'. Placeholder '" + unreplacedPlaceholder + "' was not resolved.");
        }
        return result;
    }


    private ApiErrorResponse buildInternalServerErrorResponse(Exception ex, String logMessage) {
        log.error(logMessage, ex);
        String specificDefaultMessage = "An unexpected internal error occurred.";
        if (ex instanceof IllegalStateException && ex.getMessage() != null && ex.getMessage().contains("Failed to interpolate")) {
            specificDefaultMessage = ex.getMessage();
        }
        ErrorDetail errorDetail = new ErrorDetail(MessageKeys.Error.UNEXPECTED, specificDefaultMessage, null);
        return ApiErrorResponse.forGeneralError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", errorDetail);
    }


}