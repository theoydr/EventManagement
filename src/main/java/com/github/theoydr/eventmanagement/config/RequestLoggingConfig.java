package com.github.theoydr.eventmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.Set;

/**
 * Configuration for logging incoming HTTP requests.
 * This filter automatically logs the request URL, payload, client IP, etc.
 * It provides visibility into the raw traffic hitting the API.
 */
@Configuration
public class RequestLoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingConfig.class);


    @Bean
    public CommonsRequestLoggingFilter logFilter(@Value("${logging.api.include-payload:false}") boolean includePayload) {

        NoiseFilteringRequestLoggingFilter filter = getRequestLoggingFilter(includePayload);

        if (includePayload) {
            log.warn("!!! WARNING: HTTP REQUEST PAYLOAD LOGGING IS ENABLED !!!");
            log.warn("This configuration is intended for DEVELOPMENT only. It may leak sensitive data (passwords) and reduce performance.");
            log.warn("Ensure 'logging.api.include-payload' is set to 'false' in production.");

        } else {
            log.info("HTTP Request logging initialized. Payloads are excluded (Safe Mode).");
        }

        return filter;
    }

    private static NoiseFilteringRequestLoggingFilter getRequestLoggingFilter(boolean includePayload) {
        NoiseFilteringRequestLoggingFilter filter = new NoiseFilteringRequestLoggingFilter();

        // Include the query string (e.g., ?page=1&sort=desc)
        filter.setIncludeQueryString(true);

        // Do not log the payload (body).
        // This avoids security risks (passwords/PII) and saves disk space/performance in production.
        filter.setIncludePayload(includePayload);

        // Set a max length just in case payload logging is enabled later via properties
        filter.setMaxPayloadLength(10000);

        // Include headers (like User-Agent) - keep false to reduce noise
        filter.setIncludeHeaders(false);

        filter.setIncludeClientInfo(true); // Log client IP address
        return filter;
    }


    private static class NoiseFilteringRequestLoggingFilter extends CommonsRequestLoggingFilter {

        // Paths we generally don't care about seeing in the logs every 30 seconds
        private static final Set<String> NOISY_PATHS = Set.of(
                "/actuator/health",
                "/actuator/prometheus",
                "/favicon.ico",
                "/v3/api-docs"
        );

        @Override
        protected boolean shouldLog(HttpServletRequest request) {
            // Check the Blocklist: Is this a known noisy path?

            String path = request.getRequestURI();


            for (String prefix : NOISY_PATHS) {
                if (path.startsWith(prefix)) {
                    return false;
                }
            }

            return super.shouldLog(request);
        }
    }

}
