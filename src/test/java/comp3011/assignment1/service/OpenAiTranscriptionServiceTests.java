package comp3011.assignment1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.mock.web.MockMultipartFile;

import comp3011.assignment1.dto.GlobalStatsResponse;

/*
 * Tests the transcription service without contacting
 * the real OpenAI API.
 */
@ExtendWith(OutputCaptureExtension.class)
class OpenAiTranscriptionServiceTests {

    private MockRestServiceServer mockOpenAiServer;
    private GlobalStatisticsService statisticsService;
    private OpenAiTranscriptionService transcriptionService;

    @BeforeEach
    void setUp() {

        /*
         * Create a RestClient connected to a fake OpenAI server.
         * The mock server intercepts requests from this client.
         */
        RestClient.Builder clientBuilder =
                RestClient.builder()
                        .baseUrl("https://openai.test");

        mockOpenAiServer =
                MockRestServiceServer
                        .bindTo(clientBuilder)
                        .build();

        statisticsService = new GlobalStatisticsService();

        transcriptionService =
                new OpenAiTranscriptionService(
                        clientBuilder.build(),
                        statisticsService,
                        "test-transcription-model");
    }

    @Test
    void returnsTranscriptionAndRecordsTokens() {

        // Create the JSON that the fake OpenAI server will return
        String responseJson = """
                {
                  "text": "Hello from the test.",
                  "usage": {
                    "input_tokens": 12,
                    "output_tokens": 4
                  }
                }
                """;

        mockOpenAiServer
                .expect(requestTo(
                        "https://openai.test/audio/transcriptions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        responseJson,
                        MediaType.APPLICATION_JSON));

        MockMultipartFile audioFile =
                createAudioFile();

        String transcription =
                transcriptionService.transcribe(audioFile);

        GlobalStatsResponse totals =
                statisticsService.getGlobalStats();

        // Check the returned text and updated token totals
        assertEquals(
                "Hello from the test.",
                transcription);

        assertEquals(12, totals.inputTokens());
        assertEquals(4, totals.outputTokens());

        mockOpenAiServer.verify();
    }

    @Test
    void rejectsEmptyAudio() {

        MockMultipartFile emptyAudio =
                new MockMultipartFile(
                        "audio",
                        "recording.webm",
                        "audio/webm",
                        new byte[0]);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transcriptionService.transcribe(
                                emptyAudio));

        assertEquals(
                "An audio file must be provided.",
                exception.getMessage());
    }

    @Test
    void rejectsResponseWithoutTranscription() {

        // The response contains no transcription text
        mockOpenAiServer
                .expect(requestTo(
                        "https://openai.test/audio/transcriptions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON));

        assertThrows(
                IllegalStateException.class,
                () -> transcriptionService.transcribe(
                        createAudioFile()));

        GlobalStatsResponse totals =
                statisticsService.getGlobalStats();

        // Invalid responses must not change the statistics
        assertEquals(0, totals.inputTokens());
        assertEquals(0, totals.outputTokens());

        mockOpenAiServer.verify();
    }

    @Test
    void logsExternalServiceFailure(
            CapturedOutput output) {

        mockOpenAiServer
                .expect(requestTo(
                        "https://openai.test/audio/transcriptions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(
                        HttpStatus.TOO_MANY_REQUESTS));

        RestClientResponseException exception =
                assertThrows(
                        RestClientResponseException.class,
                        () -> transcriptionService.transcribe(
                                createAudioFile()));

        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS,
                exception.getStatusCode());

        /*
         * The status is useful for debugging, but no external
         * response content or authentication data is logged.
         */
        assertTrue(output.getAll().contains(
                "OpenAI transcription request failed with status 429"));

        mockOpenAiServer.verify();
    }

    private MockMultipartFile createAudioFile() {

        return new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "fake audio data".getBytes(
                        StandardCharsets.UTF_8));
    }
}