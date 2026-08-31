package comp3011.assignment1.service;

// server start time when Spring creates it
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import comp3011.assignment1.dto.UptimeResponse;

@Service
public class ServerUptimeService {

    private final Clock clock;
    private final Instant utcServerStart;

    public ServerUptimeService(Clock clock) {
        this.clock = clock;
        this.utcServerStart = clock.instant();
    }

    public UptimeResponse getUptime() {

        Instant utcNow = clock.instant();

        double serverUptimeSeconds =
                Duration.between(utcServerStart, utcNow).toNanos()
                / 1_000_000_000.0;

        return new UptimeResponse(
                utcServerStart,
                utcNow,
                serverUptimeSeconds);
    }

}
