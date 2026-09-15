package comp3011.assignment1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestClientResponseException;

import comp3011.assignment1.dto.OpenAiTranscriptionResponse;

// Handles communication with OpenAI transcription API and records token usage
@Service
public class OpenAiTranscriptionService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OpenAiTranscriptionService.class);

    private final RestClient openAiRestClient;
    private final GlobalStatisticsService globalStatisticsService;
    private final String transcriptionModel;

    // Receives the configured client, statistics service and model
    public OpenAiTranscriptionService(
            RestClient openAiRestClient,
            GlobalStatisticsService globalStatisticsService,
            @Value("${openai.transcription-model}")
            String transcriptionModel) {

        this.openAiRestClient = openAiRestClient;
        this.globalStatisticsService = globalStatisticsService;
        this.transcriptionModel = transcriptionModel;
    }

    // Sends an uploaded audio file to OpenAI
    public String transcribe(MultipartFile audioFile) {
    	
    	// Reject invalid uploads before making extenral API requests
        if (audioFile == null || audioFile.isEmpty()) {
            LOGGER.warn(
                    "Transcription request rejected because the audio was empty");

            throw new IllegalArgumentException(
                    "An audio file must be provided.");
        }

        /*
         * Only safe request information is logged.
         * The filename, audio contents and API key are not included.
         */
        LOGGER.info(
                "Sending audio to OpenAI transcription service "
                + "(size={} bytes, contentType={})",
                audioFile.getSize(),
                audioFile.getContentType());

        // Build the multipart form required by OpenAI
        MultiValueMap<String, Object> requestBody =
                new LinkedMultiValueMap<>();

        requestBody.add("file", audioFile.getResource());
        requestBody.add("model", transcriptionModel);
        requestBody.add("response_format", "json");

        OpenAiTranscriptionResponse response;

        try {
            // Send the audio and map the JSON response to a Java record
            response = openAiRestClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiTranscriptionResponse.class);

        } catch (RestClientResponseException exception) {

            /*
             * Log only the status code. The external response body
             * could contain information that should not be logged.
             */
            LOGGER.warn(
                    "OpenAI transcription request failed with status {}",
                    exception.getStatusCode());

            throw exception;

        } catch (RuntimeException exception) {

            // Record the error type without logging sensitive request data
            LOGGER.error(
                    "OpenAI transcription request failed because of {}",
                    exception.getClass().getSimpleName());

            throw exception;
        }

        // Ensure OpenAI returned transcription text
        if (response == null || response.text() == null) {
            LOGGER.error(
                    "OpenAI returned an invalid transcription response");

            throw new IllegalStateException(
                    "OpenAI did not return a transcription.");
        }

        // Update the thread-safe totals when usage information is available
        if (response.usage() != null) {
            globalStatisticsService.recordUsage(
                    response.usage().inputTokens(),
                    response.usage().outputTokens());
        }

        LOGGER.info(
                "OpenAI transcription request completed successfully");

        return response.text();
    }
}