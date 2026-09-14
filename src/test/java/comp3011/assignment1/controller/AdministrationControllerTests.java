package comp3011.assignment1.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import comp3011.assignment1.dto.UptimeResponse;
import comp3011.assignment1.service.GracefulShutdownService;
import comp3011.assignment1.service.ServerUptimeService;

// Loads only the AdministrationController and required web components
@WebMvcTest(AdministrationController.class)
class AdministrationControllerTests {

    // Sends fake HTTP requests to the controller
    @Autowired
    private MockMvc mockMvc;

    // Replaces the real uptime service during testing
    @MockitoBean
    private ServerUptimeService serverUptimeService;

    // Replaces the shutdown service so the application does not actually stop
    @MockitoBean
    private GracefulShutdownService gracefulShutdownService;

    @Test
    void returnsServerUptime() throws Exception {

        // Uses fixed times so the expected test result does not change
        Instant serverStart =
                Instant.parse("2026-09-12T00:00:00Z");

        Instant currentTime =
                Instant.parse("2026-09-12T00:00:05Z");

        // Sets the response that the fake uptime service should return
        when(serverUptimeService.getUptime())
                .thenReturn(new UptimeResponse(
                        serverStart,
                        currentTime,
                        5.0));

        // Checks that the uptime endpoint returns the expected values
        mockMvc.perform(get("/api/v1/admin/uptime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utcServerStart")
                        .value("2026-09-12T00:00:00Z"))
                .andExpect(jsonPath("$.utcNow")
                        .value("2026-09-12T00:00:05Z"))
                .andExpect(jsonPath("$.serverUptimeSeconds")
                        .value(5.0));
    }

    @Test
    void acceptsFirstShutdownRequest() throws Exception {

        // Makes the fake service accept the first shutdown request
        when(gracefulShutdownService.requestShutdown())
                .thenReturn(true);

        // Checks that the accepted request returns status 202
        mockMvc.perform(post("/api/v1/admin/shutdown"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message")
                        .value("Graceful shutdown requested."));
    }

    @Test
    void returnsConflictWhenShutdownAlreadyRequested()
            throws Exception {

        // Returning false means a shutdown is already in progress
        when(gracefulShutdownService.requestShutdown())
                .thenReturn(false);

        // Checks that another shutdown request returns a 409 conflict
        mockMvc.perform(post("/api/v1/admin/shutdown"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Graceful shutdown is already in progress."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/admin/shutdown"));
    }

    @Test
    void returnsInternalServerErrorWhenShutdownFails()
            throws Exception {

        // Simulates an unexpected problem inside the shutdown service
        when(gracefulShutdownService.requestShutdown())
                .thenThrow(new IllegalStateException(
                        "Unexpected test error"));

        // Checks that the unexpected failure returns a safe 500 response
        mockMvc.perform(post("/api/v1/admin/shutdown"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Unable to request graceful shutdown."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/admin/shutdown"));
    }
}