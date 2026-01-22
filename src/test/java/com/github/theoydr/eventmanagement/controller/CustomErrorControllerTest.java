package com.github.theoydr.eventmanagement.controller;

import com.github.theoydr.eventmanagement.constants.MessageKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(CustomErrorController.class)
class CustomErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ErrorAttributes errorAttributes;

    @Test
    @DisplayName("Should return 404 with ENDPOINT_NOT_FOUND key for missing URLs")
    void handleError_404_ReturnsNotFound() throws Exception {
        // Arrange
        Map<String, Object> attributes = Map.of(
                "status", 404,
                "path", "/api/unknown-url"
        );

        // Mock Spring's internal error attributes
        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class)))
                .thenReturn(attributes);

        // Act & Assert
        mockMvc.perform(get("/error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                // Verify we are using the correct key for consistency
                .andExpect(jsonPath("$.error.key").value(MessageKeys.Error.ENDPOINT_NOT_FOUND))
                .andExpect(jsonPath("$.error.defaultMessage").value("The requested endpoint was not found."))
                .andExpect(jsonPath("$.error.arguments.path").value("/api/unknown-url"));
    }

    @Test
    @DisplayName("Should return 500 with MASKED message for Server Errors (Security Check)")
    void handleError_500_MasksInternalDetails() throws Exception {
        // Arrange
        Map<String, Object> attributes = Map.of(
                "status", 500,
                "message", "Connection refused to database: 192.168.1.5", // Sensitive info that should be hidden
                "path", "/api/events"
        );

        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class)))
                .thenReturn(attributes);

        // Act & Assert
        mockMvc.perform(get("/error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("500 INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.key").value(MessageKeys.Error.UNEXPECTED))
                // CRITICAL: Ensure sensitive message is NOT returned to client
                .andExpect(jsonPath("$.error.defaultMessage").value("An unexpected internal error occurred."));
    }

    @Test
    @DisplayName("Should return 400 with original message for Client Errors (e.g. Bad Request from Filter)")
    void handleError_400_ReturnsOriginalMessage() throws Exception {
        // Arrange
        String clientErrorMsg = "Invalid header format";
        Map<String, Object> attributes = Map.of(
                "status", 400,
                "message", clientErrorMsg,
                "path", "/api/events"
        );

        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class)))
                .thenReturn(attributes);

        // Act & Assert
        mockMvc.perform(get("/error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.error.key").value(MessageKeys.Error.BAD_REQUEST))
                // For 4xx errors, we trust the message is safe to show
                .andExpect(jsonPath("$.error.defaultMessage").value(clientErrorMsg));
    }

    @Test
    @DisplayName("Should trigger Safety Net (Hardcoded 500) if controller logic itself fails")
    void handleError_SafetyNet_CatchesInternalException() throws Exception {
        // Arrange: Simulate the ErrorAttributes bean failing (e.g. throwing NPE) inside the controller
        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class)))
                .thenThrow(new RuntimeException("Something broke inside the error controller"));

        // Act & Assert
        mockMvc.perform(get("/error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                // Verify the hardcoded safety message
                .andExpect(jsonPath("$.status").value("500 INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.key").value(MessageKeys.Error.UNEXPECTED))
                .andExpect(jsonPath("$.message").value("Critical system failure during error handling"));
    }
}