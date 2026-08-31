package comp3011.assignment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.dto.UptimeResponse;
import comp3011.assignment1.service.ServerUptimeService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdministrationController {

    private final ServerUptimeService serverUptimeService;

    public AdministrationController(
            ServerUptimeService serverUptimeService) {

        this.serverUptimeService = serverUptimeService;
    }

    @GetMapping("/uptime")
    public UptimeResponse getServerUptime() {
        return serverUptimeService.getUptime();
    }
}