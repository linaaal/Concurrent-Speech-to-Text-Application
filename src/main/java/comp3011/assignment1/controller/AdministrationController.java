package comp3011.assignment1.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.dto.ErrorResponse;
import comp3011.assignment1.dto.ShutdownResponse;
import comp3011.assignment1.dto.UptimeResponse;
import comp3011.assignment1.service.GracefulShutdownService;
import comp3011.assignment1.service.ServerUptimeService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/admin")
public class AdministrationController {

    private final ServerUptimeService serverUptimeService;
    private final GracefulShutdownService gracefulShutdownService;

    public AdministrationController(
            ServerUptimeService serverUptimeService,
            GracefulShutdownService gracefulShutdownService) {

        this.serverUptimeService = serverUptimeService;
        this.gracefulShutdownService = gracefulShutdownService;
    }

    // handles GET /api/v1/admin/uptime
    @GetMapping("/uptime")
    public UptimeResponse getServerUptime() {
        return serverUptimeService.getUptime();
    }
    
    // handles POST /api/v1/admin/shutdown
    @PostMapping("/shutdown")
    public ResponseEntity<Object> requestShutdown(
    		HttpServletRequest request) {
    	
    	try {
    		boolean accepted =
    				gracefulShutdownService.requestShutdown();
    		
            if (!accepted) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(
                                Instant.now(),
                                HttpStatus.CONFLICT.value(),
                                HttpStatus.CONFLICT.getReasonPhrase(),
                                "Graceful shutdown is already in progress.",
                                request.getRequestURI()));
            }

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(new ShutdownResponse(
                            "Graceful shutdown requested."));
        } catch (RuntimeException exception) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            Instant.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            "Unable to request graceful shutdown.",
                            request.getRequestURI()));
        }
    }
}