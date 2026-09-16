package comp3011.assignment1.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import comp3011.assignment1.dto.GlobalStatsResponse;
import comp3011.assignment1.service.GlobalStatisticsService;

/**
 * Checks that the global statistics endpoint returns the current
 * input and output token totals.
 *
 * Passing this test gives confidence that stored usage information
 * is correctly exposed through the REST API.
 */

// Loads only the GlobalStatisticsController and required web components
@WebMvcTest(GlobalStatisticsController.class)
class GlobalStatisticsControllerTests {

    // Sends fake HTTP requests to the controller
    @Autowired
    private MockMvc mockMvc;

    // Replaces the real statistics service during testing
    @MockitoBean
    private GlobalStatisticsService globalStatisticsService;

    @Test
    void returnsGlobalTokenTotals() throws Exception {

        // Sets the token totals that the fake service should return
        when(globalStatisticsService.getGlobalStats())
                .thenReturn(new GlobalStatsResponse(
                        120,
                        30));

        // Checks that the endpoint returns the expected token totals
        mockMvc.perform(get("/api/v1/global/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputTokens")
                        .value(120))
                .andExpect(jsonPath("$.outputTokens")
                        .value(30));
    }
}