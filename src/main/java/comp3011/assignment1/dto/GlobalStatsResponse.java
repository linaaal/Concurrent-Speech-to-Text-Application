package comp3011.assignment1.dto;

/*
 * Represents the total OpenAI token usage since the server started.
 * Both values begin at zero and increase after successful transcriptions.
 */
public record GlobalStatsResponse(
        long inputTokens,
        long outputTokens) {
}