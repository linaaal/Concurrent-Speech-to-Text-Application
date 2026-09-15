package comp3011.assignment1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.dto.OpenAiTranscriptionResponse;

// Handles communication with OpenAI transcription API and records token usage

@Service
public class OpenAiTranscriptionService {

    private final RestClient openAiRestClient;
    private final GlobalStatisticsService globalStatisticsService;
    private final String transcriptionModel;

    // Uses constructor injection to receive configured OpenAI client, stat service and transcription model.

    public OpenAiTranscriptionService(
            RestClient openAiRestClient,
            GlobalStatisticsService globalStatisticsService,
            @Value("${openai.transcription-model}")
            String transcriptionModel) {

        this.openAiRestClient = openAiRestClient;
        this.globalStatisticsService = globalStatisticsService;
        this.transcriptionModel = transcriptionModel;
    }

    // Sends an uploaded audio file to OpenAI and returns resulting transcription.

    public String transcribe(MultipartFile audioFile) {

        // Reject invalid uploads before making extenral API requests
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "An audio file must be provided.");
        }

        // Build multipart form required by the transcription API

        MultiValueMap<String, Object> requestBody =
                new LinkedMultiValueMap<>();

        requestBody.add("file", audioFile.getResource());
        requestBody.add("model", transcriptionModel);
        requestBody.add("response_format", "json");

        // Send the audio and map JSON response to Java record

        OpenAiTranscriptionResponse response =
                openAiRestClient.post()
                        .uri("/audio/transcriptions")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(requestBody)
                        .retrieve()
                        .body(OpenAiTranscriptionResponse.class);

        // Ensure a valid transcription was returned

        if (response == null || response.text() == null) {
            throw new IllegalStateException(
                    "OpenAI did not return a transcription.");
        }

        // Update the thread-safe totals when usage information is available

        if (response.usage() != null) {
            globalStatisticsService.recordUsage(
                    response.usage().inputTokens(),
                    response.usage().outputTokens());
        }

        return response.text();
    }
}