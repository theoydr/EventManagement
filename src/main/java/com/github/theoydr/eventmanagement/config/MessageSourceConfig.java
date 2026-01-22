package com.github.theoydr.eventmanagement.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Configuration class for creating a custom MessageSource bean.
 * This implementation uses a PathMatchingResourcePatternResolver to automatically
 * discover all message bundles in the classpath, allowing for a scalable,
 * directory-based organization of property files.
 */
@Configuration
public class MessageSourceConfig {

    private static final String MESSAGES_PATTERN = "classpath*:messages/**/messages.properties";

    @Bean
    public MessageSource messageSource() throws IOException {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheSeconds(60); // Cache for 60 seconds
        messageSource.setUseCodeAsDefaultMessage(false);


        // Use PathMatchingResourcePatternResolver to find all matching files
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(MESSAGES_PATTERN);

        // Convert the resource paths into the "basename" format that MessageSource expects.
        // For example, a file at ".../resources/messages/user/messages_es.properties"
        // will be converted to the basename "classpath:messages/user/messages".
        String[] baseNames = Stream.of(resources)
                .map(this::resourceToBasename)
                .distinct()
                .filter(Objects::nonNull)
                .toArray(String[]::new);


        messageSource.setBasenames(baseNames);
        return messageSource;
    }

    /**
     * Converts a resource's full path into a basename format.
     * It removes the "_en_US.properties" or ".properties" suffix.
     *
     * @param resource The resource to convert.
     * @return The basename string.
     */
    private String resourceToBasename(Resource resource) {
        try {
            String path = resource.getURI().toString();
            // Find the last occurrence of "/messages" and take the path up to that point, plus "/messages" itself.
            // This correctly handles paths from JARs and file systems.
            int messagesIndex = path.lastIndexOf("/messages");

            // Return the path up to and including the base name 'messages'
            if (messagesIndex == -1) {
                return null;
            }

            return "classpath:" + path.substring(path.indexOf("messages/"), messagesIndex + "/messages".length());



        } catch (IOException e) {
            throw new RuntimeException("Failed to determine basename for resource: " + resource, e);
        }
    }
}
