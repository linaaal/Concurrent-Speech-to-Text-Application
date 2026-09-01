package comp3011.assignment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.dto.GlobalStatsResponse;
import comp3011.assignment1.service.GlobalStatisticsService;

@RestController
@RequestMapping("/api/v1/global")
public class GlobalStatisticsController {

    private final GlobalStatisticsService globalStatisticsService;

    // Spring provides the statistics service through constructor injection
    public GlobalStatisticsController(
            GlobalStatisticsService globalStatisticsService) {

        this.globalStatisticsService = globalStatisticsService;
    }

    // Handles GET /api/v1/global/stats
    @GetMapping("/stats")
    public GlobalStatsResponse getGlobalStats() {
        return globalStatisticsService.getGlobalStats();
    }
}