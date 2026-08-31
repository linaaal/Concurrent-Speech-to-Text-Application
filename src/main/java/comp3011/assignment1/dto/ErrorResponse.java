package comp3011.assignment1.dto;

import java.time.Instant;

// contains the information returned when an API error occurs
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path) {
}