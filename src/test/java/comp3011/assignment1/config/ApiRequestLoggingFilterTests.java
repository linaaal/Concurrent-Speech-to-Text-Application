package comp3011.assignment1.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(OutputCaptureExtension.class)
class ApiRequestLoggingFilterTests {

    private final ApiRequestLoggingFilter loggingFilter =
            new ApiRequestLoggingFilter();

    @Test
    void logsApiRequestResult(CapturedOutput output)
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/v1/admin/uptime");

        request.addHeader(
                "Authorization",
                "Bearer test-secret-value");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        loggingFilter.doFilter(
                request,
                response,
                new MockFilterChain());

        String logs = output.getAll();

        // Check that the useful request details were logged
        assertTrue(logs.contains(
                "API request: GET /api/v1/admin/uptime -> 200"));

        // Check that the fake secret was not logged
        assertFalse(logs.contains("test-secret-value"));
    }

    @Test
    void doesNotLogNonApiRequest(CapturedOutput output)
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        loggingFilter.doFilter(
                request,
                response,
                new MockFilterChain());

        // Normal webpage requests should not be logged
        assertFalse(output.getAll().contains("API request:"));
    }
}