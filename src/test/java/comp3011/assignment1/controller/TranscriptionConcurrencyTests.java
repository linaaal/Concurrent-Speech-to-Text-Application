package comp3011.assignment1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.service.OpenAiTranscriptionService;

/**
 * Regression test for the requirement to handle more than 200
 * simultaneous blocking HTTP requests.
 *
 * The test uses 220 requests because this is clearly above the required
 * minimum. A stub transcription service blocks each request for two
 * seconds, making the test repeatable without contacting OpenAI or using
 * an API key.
 *
 * The five-second limit allows time for the two-second simulated delay
 * and normal HTTP overhead. It would still reveal a design that processes
 * the requests in large sequential batches.
 *
 * Expected results:
 * - all 220 requests return HTTP 200;
 * - more than 200 requests are active at the same time;
 * - all requests finish in less than five seconds;
 * - the application completes without crashing.
 *
 * Test environment and measured result:
 * - tested on 15 September 2026;
 * - MacBook Pro (Mac15,3);
 * - Apple M3 with 8 cores;
 * - 16 GB memory;
 * - macOS 15.7.4;
 * - Java 25.0.3 selected by Spring Tools for Maven;
 * - Maven reported a test-class duration of 2.476 seconds.
 *
 * Passing this test provides evidence that Spring Boot virtual threads
 * can handle the required number of blocking requests without a
 * significant delay. Performance may differ on other machines.
 */

// Starts the full application on a random port using a fake API key
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "openai.api-key=test-key")
class TranscriptionConcurrencyTests {

    // Uses 220 requests to prove that the application can handle more than 200
    private static final int REQUEST_COUNT = 220;

    @LocalServerPort
    private int port;

    // Prevents the test from contacting the real OpenAI service
    @MockitoBean
    private OpenAiTranscriptionService transcriptionService;

    @Test
    void handlesMoreThan200BlockingRequests()
            throws Exception {

        // Tracks the current and highest number of simultaneous requests
        AtomicInteger activeRequests = new AtomicInteger();
        AtomicInteger highestConcurrentRequests =
                new AtomicInteger();

        /*
         * Makes each fake transcription take two seconds so requests overlap.
         * The counters record how many requests reach the service together.
         */
        when(transcriptionService.transcribe(
                any(MultipartFile.class)))
                .thenAnswer(invocation -> {
                    int active =
                            activeRequests.incrementAndGet();

                    highestConcurrentRequests.accumulateAndGet(
                            active,
                            Math::max);

                    try {
                        Thread.sleep(2000);
                        return "Test transcription";
                    } finally {
                        activeRequests.decrementAndGet();
                    }
                });

        // Sends real HTTP requests to the application’s random test port
        RestClient client = RestClient.create(
                "http://localhost:" + port);

        // Runs each request using its own virtual thread
        ExecutorService executor =
                Executors.newVirtualThreadPerTaskExecutor();

        // Holds all requests until they are ready to start together
        CountDownLatch startSignal = new CountDownLatch(1);

        List<Future<HttpStatusCode>> responses =
                new ArrayList<>();

        try {
            for (int request = 0;
                    request < REQUEST_COUNT;
                    request++) {

                responses.add(executor.submit(() -> {
                    startSignal.await();

                    MultiValueMap<String, Object> requestBody =
                            new LinkedMultiValueMap<>();

                    ByteArrayResource audioFile =
                            new ByteArrayResource(
                                    "fake audio".getBytes(
                                            StandardCharsets.UTF_8)) {

                                // Gives the uploaded resource a filename
                                @Override
                                public String getFilename() {
                                    return "recording.webm";
                                }
                            };

                    requestBody.add("audio", audioFile);

                    // Sends a real multipart request to the controller
                    return client.post()
                            .uri("/api/v1/transcriptions")
                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA)
                            .body(requestBody)
                            .retrieve()
                            .toBodilessEntity()
                            .getStatusCode();
                }));
            }

            long startTime = System.nanoTime();
            long deadline = startTime
                    + TimeUnit.SECONDS.toNanos(5);

            // Releases all 220 requests at the same time
            startSignal.countDown();

            // Checks that every request succeeds before the deadline
            for (Future<HttpStatusCode> response : responses) {
                long remainingTime =
                        deadline - System.nanoTime();

                assertTrue(
                        remainingTime > 0,
                        "Requests took longer than five seconds.");

                assertEquals(
                        HttpStatus.OK,
                        response.get(
                                remainingTime,
                                TimeUnit.NANOSECONDS));
            }

            long elapsedMilliseconds =
                    TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startTime);

            // Proves that more than 200 requests reached the service together
            assertTrue(
                    highestConcurrentRequests.get() > 200,
                    "Only "
                            + highestConcurrentRequests.get()
                            + " requests ran concurrently.");

            // Checks that all requests finished within five seconds
            assertTrue(
                    elapsedMilliseconds < 5000,
                    "Requests took "
                            + elapsedMilliseconds
                            + " milliseconds.");

        } finally {
            // Stops any remaining virtual threads after the test
            executor.shutdownNow();
        }
    }
}