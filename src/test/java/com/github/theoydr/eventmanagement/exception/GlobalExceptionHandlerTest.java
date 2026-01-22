package com.github.theoydr.eventmanagement.exception;

import com.github.theoydr.eventmanagement.config.MessageSourceConfig;
import com.github.theoydr.eventmanagement.constants.MessageKeys;
import com.github.theoydr.eventmanagement.controller.TestController;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.ObjectError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestController.class)
@Import({
        GlobalExceptionHandler.class,
        MessageSourceConfig.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext; // allows us to get beans

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;


    @Test
    @DisplayName("Should handle ResourceNotFoundException correctly")
    void test_handleResourceNotFoundException() throws Exception {
        // Arrange
        String key = MessageKeys.Error.RESOURCE_NOT_FOUND.replaceAll("[{}]", "");

        // Act & Assert
        mockMvc.perform(get("/test/throw/resource-not-found").locale(Locale.ROOT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.key").value(key))
                .andExpect(jsonPath("$.error.defaultMessage").value("User not found with id: 1"))
                .andExpect(jsonPath("$.error.arguments.resourceType").value("User"));
    }

    @Test
    @DisplayName("Should handle UserAlreadyExistsException (409) correctly")
    void test_handleUserAlreadyExistsException() throws Exception {
        // Arrange
        String key = MessageKeys.Error.USER_EXISTS.replaceAll("[{}]", "");

        // Act & Assert
        mockMvc.perform(get("/test/throw/user-exists").locale(Locale.ROOT))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.key").value(key))
                .andExpect(jsonPath("$.error.defaultMessage").value("User with email test@email.com already exists."))
                .andExpect(jsonPath("$.error.arguments.email").value("test@email.com"));
    }

    @Test
    @DisplayName("Should handle DuplicateEventException (409) correctly")
    void test_handleDuplicateEventException() throws Exception {
        String key = MessageKeys.Error.EVENT_DUPLICATE.replaceAll("[{}]", "");

        // Act & Assert
        mockMvc.perform(get("/test/throw/duplicate-event").locale(Locale.ROOT))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.key").value(key))
                .andExpect(jsonPath("$.error.defaultMessage").value("An event with the same organizer, start time, and location already exists."))
                .andExpect(jsonPath("$.error.arguments").doesNotExist());
    }

    @Test
    @DisplayName("Should handle OperationNotAllowedException (403) correctly")
    void test_handleOperationNotAllowedException() throws Exception {
        // Arrange
        String key = MessageKeys.Error.OPERATION_NOT_ALLOWED.replaceAll("[{}]", "");

        // Act & Assert
        mockMvc.perform(get("/test/throw/operation-not-allowed").locale(Locale.ROOT))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.key").value(key))
                .andExpect(jsonPath("$.error.defaultMessage").value("You do not have the required permissions to perform this action."))
                .andExpect(jsonPath("$.error.arguments").doesNotExist());

    }

    @Test
    @DisplayName("Should handle EventBookingException (409) correctly")
    void test_handleEventBookingException() throws Exception {
        // Arrange
        String key = MessageKeys.Error.BOOKING_FAILED.replaceAll("[{}]", "");
        // Act & Assert
        mockMvc.perform(get("/test/throw/event-booking-exception").locale(Locale.ROOT))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.key").value(key))
                .andExpect(jsonPath("$.error.defaultMessage").value("Booking failed. Reason: EVENT_IN_PAST"))
                .andExpect(jsonPath("$.error.arguments.reasonCode").value("EVENT_IN_PAST"));
    }

    @Test
    @DisplayName("Should handle NoResourceFoundException (404) correctly")
    void test_handleNoResourceFoundException() throws Exception {
        // Arrange
        String key = MessageKeys.Error.ENDPOINT_NOT_FOUND.replaceAll("[{}]", "");
        String invalidPath = "/test/this-endpoint-does-not-exist";


        // Act & Assert
        mockMvc.perform(get(invalidPath).locale(Locale.ROOT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.key").value(key))
                .andExpect(jsonPath("$.error.defaultMessage").value("The requested endpoint '/test/this-endpoint-does-not-exist' was not found."))
                .andExpect(jsonPath("$.error.arguments.path").value(invalidPath));
    }


    @Test
    @DisplayName("Should handle generic Exception (500) correctly")
    void test_handleAllUncaughtException() throws Exception {
        // Arrange
        String key = MessageKeys.Error.UNEXPECTED.replaceAll("[{}]", "");

        // Act & Assert
        mockMvc.perform(get("/test/throw/runtime-exception").locale(Locale.ROOT))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.key").value(key))
                .andExpect(jsonPath("$.error.defaultMessage").value("An unexpected internal error occurred."))
                .andExpect(jsonPath("$.error.arguments").doesNotExist());

    }

    @Test
    @DisplayName("Should handle JSON Type Mismatch (DatabindException) and extract field name")
    void test_handleHttpMessageNotReadableException_withMalformedField() throws Exception {
        // Arrange
        String key = MessageKeys.Error.INVALID_FORMAT.replaceAll("[{}]", "");
        String jsonPayload = "{\"number\": \"abc\"}";

        // Act & Assert
        mockMvc.perform(post("/test/throw/type-mismatch").locale(Locale.ROOT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.key").value(key))
                .andExpect(jsonPath("$.message").value(containsString("Malformed JSON")))
                .andExpect(jsonPath("$.error.defaultMessage").value("The format is invalid"))
                .andExpect(jsonPath("$.error.arguments.field").value("number"));

    }


    @Test
    @DisplayName("Should handle Malformed JSON (Syntax Error) without field extraction")
    void test_handleHttpMessageNotReadableException_withSyntaxError() throws Exception {
        // Arrange
        String key = MessageKeys.Error.INVALID_FORMAT.replaceAll("[{}]", "");
        String jsonPayload = "{\"value\": \"test\"";

        // Act & Assert
        mockMvc.perform(post("/test/throw/type-mismatch").locale(Locale.ROOT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.key").value(key))
                .andExpect(jsonPath("$.message").value(containsString("Malformed JSON")))
                .andExpect(jsonPath("$.error.defaultMessage").value("The format is invalid"))
                .andExpect(jsonPath("$.error.arguments.field").doesNotExist());

    }

    @Test
    @DisplayName("Should handle validation error correctly")
    void test_handleValidationException() throws Exception {
        // Arrange
        String key = MessageKeys.Error.INVALID_FORMAT.replaceAll("[{}]", "");

        // Act & Assert
        mockMvc.perform(post("/test/throw/validation").locale(Locale.ROOT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.value.defaultMessage").value("The format is invalid"))
                .andExpect(jsonPath("$.fieldErrors.value.key").value(key));

    }



    @Test
    @DisplayName("getDefaultMessage returns fallback when interpolation fails")
    void test_getDefaultMessage_interpolationFails() throws Exception {
        // Spy the real MessageSource
        MessageSource realMessageSource = applicationContext.getBean(MessageSource.class);
        MessageSource spyMessageSource = spy(realMessageSource);
        doReturn("Hello {missing}").when(spyMessageSource)
                .getMessage(eq("test.key"), any(), any(), eq(Locale.ROOT));

        // Replace the messageSource in the handler
        ReflectionTestUtils.setField(globalExceptionHandler, "messageSource", spyMessageSource);

        // Use reflection to invoke the private method
        Method method = GlobalExceptionHandler.class.getDeclaredMethod("getDefaultMessage", Map.class, String.class);
        method.setAccessible(true);

        String defaultMessage = (String) method.invoke(globalExceptionHandler, Map.of(), "test.key");

        assertEquals("Fallback Message", defaultMessage);
    }

    @Test
    @DisplayName("Interpolation failure triggers fallback message")
    void test_interpolateMessage_failureReturnsFallback() throws Exception {
        String template = "Hello {missing}";
        Map<String, Object> args = Map.of(); // empty args to force failure

        // Access private method via reflection
        Method method = GlobalExceptionHandler.class
                .getDeclaredMethod("interpolateMessage", String.class, Map.class, String.class);
        method.setAccessible(true);

        // Invoke and catch InvocationTargetException
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () ->
                method.invoke(globalExceptionHandler, template, args, "test.key")
        );

        // The actual exception thrown inside the method
        Throwable cause = thrown.getCause();
        assertNotNull(cause);
        assertInstanceOf(IllegalStateException.class, cause);
        assertTrue(cause.getMessage().contains("Placeholder 'missing' was not resolved"));
    }



    @Test
    @DisplayName("Field-level fallback is returned when createErrorDetailFromValidationError fails")
    void test_createErrorDetailFromValidationError_failsGracefully() throws Exception {
        ObjectError failingError = mock(ObjectError.class);
        when(failingError.getObjectName()).thenReturn("fieldX");

        // Force unwrap to throw
        when(failingError.unwrap(ConstraintViolation.class)).thenThrow(new RuntimeException("Boom"));

        Method method = GlobalExceptionHandler.class
                .getDeclaredMethod("createErrorDetailFromValidationError", ObjectError.class);
        method.setAccessible(true);


        ErrorDetail errorDetail = (ErrorDetail) method.invoke(globalExceptionHandler, failingError);

        assertEquals(MessageKeys.Error.UNEXPECTED.replaceAll("[{}]", ""), errorDetail.key());
        assertEquals("Unexpected error processing this validation field", errorDetail.defaultMessage());
        assertEquals("fieldX", errorDetail.arguments().get("fieldName"));
    }




}
