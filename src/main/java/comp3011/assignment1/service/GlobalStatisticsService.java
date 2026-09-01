package comp3011.assignment1.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import comp3011.assignment1.dto.GlobalStatsResponse;

@Service
public class GlobalStatisticsService {

    /*
     * Stores the total tokens used since the server started.
     * AtomicLong prevents lost updates when requests run concurrently.
     */
    private final AtomicLong inputTokens = new AtomicLong(0);
    private final AtomicLong outputTokens = new AtomicLong(0);

    // Adds the token usage from one successful transcription
    public void recordUsage(long inputTokenCount, long outputTokenCount) {

        // Token counts should never be negative
        if (inputTokenCount < 0 || outputTokenCount < 0) {
            throw new IllegalArgumentException(
                    "Token counts cannot be negative.");
        }

        inputTokens.addAndGet(inputTokenCount);
        outputTokens.addAndGet(outputTokenCount);
    }

    // Returns the current total token usage
    public GlobalStatsResponse getGlobalStats() {
        return new GlobalStatsResponse(
                inputTokens.get(),
                outputTokens.get());
    }
}