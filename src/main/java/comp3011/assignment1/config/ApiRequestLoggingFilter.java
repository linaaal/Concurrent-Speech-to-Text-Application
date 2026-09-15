package comp3011.assignment1.config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Logs the result and duration of each API request.
 * Request bodies and headers are not logged because they may
 * contain audio data or authentication information.
 */
@Component
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ApiRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.nanoTime();

        try {
            // Continue processing the request normally
            filterChain.doFilter(request, response);

        } finally {
            long durationMilliseconds =
                    TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startTime);

            LOGGER.info(
                    "API request: {} {} -> {} ({} ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMilliseconds);
        }
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        // Only log application API requests
        return !request.getRequestURI().startsWith("/api/");
    }
}