package comp3011.assignment1.dto;

import java.time.Instant;

// Stores the server start time, current time and total uptime
public record UptimeResponse(
	Instant utcServerStart,
	Instant utcNow,
	double serverUptimeSeconds) {
	
}
