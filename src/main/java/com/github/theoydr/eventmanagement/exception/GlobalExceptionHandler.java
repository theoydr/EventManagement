package com.github.theoydr.eventmanagement.exception;

import com.github.theoydr.eventmanagement.constants.MessageKeys;
import jakarta.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
    private static final Pattern MISSING_KEY_PATTERN = Pattern.compile("'([^']*)'");
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");


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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        try {
            log.warn("Validation failed: {}", ex.getMessage());

            Map<String, ErrorDetail> fieldErrors = ex.getBindingResult().getAllErrors().stream()
                    .collect(Collectors.toMap(
                            error -> (error instanceof FieldError fieldError) ? fieldError.getField() : error.getObjectName(),
                            this::createErrorDetailFromValidationError
                    ));

            return ApiErrorResponse.forValidationError(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
        } catch (NoSuchMessageException nsmEx) {

            return handleNoSuchMessageException(nsmEx);
        } catch (Exception internalEx) {

            return buildInternalServerErrorResponse(ex, "An unexpected error occurred while processing validation exceptions");

        }
    }

    private ErrorDetail createErrorDetailFromValidationError(ObjectError error) {
        ConstraintViolation<?> constraintViolation = error.unwrap(ConstraintViolation.class);

        String key = constraintViolation.getMessageTemplate().replaceAll("[{}]", "");


        Set<String> standardAttributes = Set.of("message", "groups", "payload");


        Map<String, Object> arguments = constraintViolation.getConstraintDescriptor().getAttributes();
        Map<String, Object> constraintArguments = arguments.entrySet().stream()
                .filter(entry -> !standardAttributes.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        // --- THIS IS THE FIX ---
        // 1. Look up the raw template using only the key.
        String defaultMessageTemplate = messageSource.getMessage(key, null, Locale.ROOT);
        // 2. Manually interpolate the message with our named arguments.
        String defaultMessage = interpolateMessage(defaultMessageTemplate, constraintArguments, key);
        return new ErrorDetail(key, defaultMessage, constraintArguments);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleAllUncaughtException(Exception ex) {
        return buildInternalServerErrorResponse(ex, "An unexpected exception occurred");

    }


    @ExceptionHandler(NoSuchMessageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleNoSuchMessageException(NoSuchMessageException ex) {
        String rawMessage = ex.getMessage();
        String missingKey = rawMessage; // Default to the full message


        Matcher matcher = MISSING_KEY_PATTERN.matcher(rawMessage);
        if (matcher.find()) {
            missingKey = matcher.group(1);
        }

        log.error("Missing message key configuration: {}", missingKey);

        Map<String, Object> args = Map.of("missingKey", missingKey);
        String key = MessageKeys.Error.MESSAGE_NOT_FOUND.replaceAll("[{}]", "");


        String defaultMessage;
        try {
            // Get the template for the meta-error itself.
            String metaErrorTemplate = messageSource.getMessage(key, null, Locale.ROOT);
            // Now, interpolate the template with the arguments we have.
            defaultMessage = interpolateMessage(metaErrorTemplate, args, key);
        } catch (NoSuchMessageException metaEx) {
            // Ultimate fallback if even our meta-error key is missing. This should never happen.
            defaultMessage = "FATAL ERROR: The message key '" + missingKey + "' was not found, and the key for reporting this error is also missing.";
        } catch (Exception internalEx) {
            return buildInternalServerErrorResponse(internalEx, "Failed while handling no such message exception due to an internal error");

        }
        ErrorDetail errorDetail = new ErrorDetail(key, defaultMessage, args);
        return ApiErrorResponse.forGeneralError(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred", errorDetail);


    }


    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNoResourceFoundException(NoResourceFoundException ex) {
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
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildGeneralErrorResponse(ex.getArguments(), MessageKeys.Error.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Resource not found");

    }

    /**
     * Handles the case where a user tries to register with an email that already exists.
     *
     * @param ex The UserAlreadyExistsException that was thrown.
     * @return A ResponseEntity with a 409 Conflict status and a clear error message.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return buildGeneralErrorResponse(ex.getArguments(), MessageKeys.Error.USER_EXISTS, HttpStatus.CONFLICT, "User already exists");

    }

    /**
     * Handles business logic errors related to event bookings (e.g., booking a full event).
     *
     * @param ex The EventBookingException that was thrown.
     * @return A ResponseEntity with a 400 Bad Request status and a clear error message.
     */
    @ExceptionHandler(EventBookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleEventBookingException(EventBookingException ex) {
        return buildGeneralErrorResponse(ex.getArguments(), MessageKeys.Error.BOOKING_FAILED, HttpStatus.BAD_REQUEST, "Booking failed");

    }

    @ExceptionHandler(DuplicateEventException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDuplicateEventException(DuplicateEventException ex) {
        return buildGeneralErrorResponse(ex.getArguments(), MessageKeys.Error.EVENT_DUPLICATE, HttpStatus.CONFLICT, "Duplicate event detected");

    }


    /**
     * A centralized helper method to build error responses for general business exceptions.
     * It uses the MessageSource to resolve the default message, enabling the "fail loudly"
     * strategy for all general exceptions.
     */
    private ApiErrorResponse buildGeneralErrorResponse(Map<String, Object> arguments, String templateKey, HttpStatus status, String message) {




        String key = templateKey.replaceAll("[{}]", "");
        log.warn("Business exception occurred. Status: {}, Key: {}, Message: {}, Arguments: {}", status, key, message, arguments);

        try {

            String defaultMessageTemplate = messageSource.getMessage(key, null, Locale.ROOT);
            String defaultMessage = interpolateMessage(defaultMessageTemplate, arguments, key);

            ErrorDetail errorDetail = new ErrorDetail(key, defaultMessage, arguments);
            return ApiErrorResponse.forGeneralError(status, message, errorDetail);
        } catch (NoSuchMessageException nsmEx) {
            // The key for this business exception was not found in any property file.
            // This is a server-side bug, so we delegate to the 500 error handler.
            return handleNoSuchMessageException(new NoSuchMessageException(key));
        } catch (Exception ex) {
            return buildInternalServerErrorResponse(ex, "Failed to build general error response due to an internal error");
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
        log.error("An unexpected internal error occurred.", ex);
        String specificDefaultMessage = "An unexpected internal error occurred.";
        if (ex instanceof IllegalStateException && ex.getMessage().contains("Failed to interpolate")) {
            specificDefaultMessage = ex.getMessage();
        }
        ErrorDetail errorDetail = new ErrorDetail(MessageKeys.Error.UNEXPECTED, specificDefaultMessage, null);
        return ApiErrorResponse.forGeneralError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", errorDetail);
    }


}