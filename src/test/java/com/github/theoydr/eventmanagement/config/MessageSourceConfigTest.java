package com.github.theoydr.eventmanagement.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSourceConfigTest {

    @InjectMocks
    private MessageSourceConfig messageSourceConfig;

    @Mock
    private Resource resource;

    @Test
    @DisplayName("Should correctly parse standard file system path")
    void resourceToBasename_FileSystem() throws Exception {
        // Arrange
        String filePath = "file:/C:/projects/event-flow/target/classes/messages/user/messages.properties";
        when(resource.getURI()).thenReturn(new URI(filePath));

        // Act
        // Access the private method using ReflectionTestUtils
        String basename = ReflectionTestUtils.invokeMethod(
                messageSourceConfig,
                "resourceToBasename",
                resource
        );

        // Assert
        assertThat(basename).isEqualTo("classpath:messages/user/messages");
    }

    @Test
    @DisplayName("Should correctly parse path with locale suffix (e.g., _es)")
    void resourceToBasename_WithLocale() throws Exception {
        // Arrange
        String filePath = "file:/app/classes/messages/event/messages_es.properties";
        when(resource.getURI()).thenReturn(new URI(filePath));

        // Act
        String basename = (String) ReflectionTestUtils.invokeMethod(
                messageSourceConfig,
                "resourceToBasename",
                resource
        );

        // Assert
        // It should strip the locale and extension
        assertThat(basename).isEqualTo("classpath:messages/event/messages");
    }

    @Test
    @DisplayName("Should correctly parse JAR file path")
    void resourceToBasename_JarPath() throws Exception {
        // Arrange
        // Simulating a path inside a JAR file (common in production)
        String filePath = "jar:file:/app/event-flow.jar!/BOOT-INF/classes!/messages/booking/messages.properties";
        when(resource.getURI()).thenReturn(new URI(filePath));

        // Act
        String basename = (String) ReflectionTestUtils.invokeMethod(
                messageSourceConfig,
                "resourceToBasename",
                resource
        );

        // Assert
        assertThat(basename).isEqualTo("classpath:messages/booking/messages");
    }

    @Test
    @DisplayName("Should return null if path does not contain 'messages/'")
    void resourceToBasename_InvalidPath() throws Exception {
        // Arrange
        String filePath = "file:/C:/projects/application.properties";
        when(resource.getURI()).thenReturn(new URI(filePath));

        // Act
        String basename = ReflectionTestUtils.invokeMethod(
                messageSourceConfig,
                "resourceToBasename",
                resource
        );

        // Assert
        assertThat(basename).isNull();
    }


}