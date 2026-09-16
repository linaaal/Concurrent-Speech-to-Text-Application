package comp3011.assignment1.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.service.OpenAiTranscriptionService;

/**
 * Checks the transcription REST endpoint using a stubbed
 * transcription service.
 *
 * The tests cover successful, empty and failed requests without
 * contacting OpenAI. Passing them gives confidence that the
 * controller returns the correct responses and error statuses.
 */


//Loads only the TranscriptionController and the web components needed for these tests
@WebMvcTest(TranscriptionController.class)
class TranscriptionControllerTests {

    // Sends fake HTTP requests to the controller
    @Autowired
    private MockMvc mockMvc;

    // Replaces the real OpenAI service so no real API requests are made
    @MockitoBean
    private OpenAiTranscriptionService transcriptionService;

    @Test
    void returnsTranscriptionForValidAudio() throws Exception {

        // Creates a small fake audio upload
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "fake audio data".getBytes());

        // Sets the transcription that the fake service should return
        when(transcriptionService.transcribe(
                any(MultipartFile.class)))
                .thenReturn("Hello from the test.");

        // Sends the audio to the controller and checks the successful response
        mockMvc.perform(
                multipart("/api/v1/transcriptions")
                        .file(audioFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text")
                        .value("Hello from the test."));
    }

    @Test
    void returnsBadRequestForEmptyAudio() throws Exception {

        // Creates an empty audio upload
        MockMultipartFile emptyAudioFile = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                new byte[0]);

        // Checks that the controller rejects the empty file
        mockMvc.perform(
                multipart("/api/v1/transcriptions")
                        .file(emptyAudioFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("An audio file must be provided."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/transcriptions"));
    }

    @Test
    void returnsBadGatewayWhenOpenAiFails() throws Exception {

        // Creates a valid fake audio upload
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "fake audio data".getBytes());

        /*
         * Simulates OpenAI rejecting the request with a 429 error.
         * No real request is sent to OpenAI.
         */
        when(transcriptionService.transcribe(
                any(MultipartFile.class)))
                .thenThrow(new HttpClientErrorException(
                        HttpStatus.TOO_MANY_REQUESTS));

        // Checks that the OpenAI error is converted into a safe 502 response
        mockMvc.perform(
                multipart("/api/v1/transcriptions")
                        .file(audioFile))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error")
                        .value("Bad Gateway"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "The transcription service could not process the audio."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/transcriptions"));
    }

    @Test
    void returnsInternalServerErrorForUnexpectedFailure()
            throws Exception {

        // Creates a valid fake audio upload
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "fake audio data".getBytes());

        // Simulates an unexpected problem inside the service
        when(transcriptionService.transcribe(
                any(MultipartFile.class)))
                .thenThrow(new IllegalStateException(
                        "Unexpected test error"));

        // Checks that an unexpected failure returns a safe 500 response
        mockMvc.perform(
                multipart("/api/v1/transcriptions")
                        .file(audioFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Unable to create the transcription."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/transcriptions"));
    }
}