package comp3011.assignment1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Represents the JSON returned by OpenAI after transcription.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiTranscriptionResponse(
        String text,
        Usage usage) {

    /*
     * Stores the token usage returned with the transcription.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
            @JsonProperty("input_tokens") long inputTokens,
            @JsonProperty("output_tokens") long outputTokens) {
    }
}