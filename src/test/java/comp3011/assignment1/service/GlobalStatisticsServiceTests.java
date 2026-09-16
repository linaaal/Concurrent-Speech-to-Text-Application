package comp3011.assignment1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import comp3011.assignment1.dto.GlobalStatsResponse;

/**
 * Checks that token totals remain correct when many threads update
 * them at the same time.
 *
 * The expected totals must match every completed update. Passing
 * this test provides evidence that the atomic counters prevent
 * lost updates and race conditions.
 */
class GlobalStatisticsServiceTests {

    @Test
    void recordsUsageWithoutLosingConcurrentUpdates()
            throws Exception {

        int numberOfThreads = 20;
        int updatesPerThread = 1000;

        GlobalStatisticsService statisticsService =
                new GlobalStatisticsService();

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfThreads);

        /*
         * All threads wait for this signal so they begin
         * updating the counters at approximately the same time.
         */
        CountDownLatch startSignal = new CountDownLatch(1);

        List<Future<?>> tasks = new ArrayList<>();

        try {
            for (int thread = 0;
                    thread < numberOfThreads;
                    thread++) {

                tasks.add(executor.submit(() -> {

                    startSignal.await();

                    for (int update = 0;
                            update < updatesPerThread;
                            update++) {

                        statisticsService.recordUsage(1, 2);
                    }

                    return null;
                }));
            }

            // Allow every thread to begin updating
            startSignal.countDown();

            // Wait for every task to finish
            for (Future<?> task : tasks) {
                task.get(10, TimeUnit.SECONDS);
            }

        } finally {
            executor.shutdownNow();
        }

        GlobalStatsResponse result =
                statisticsService.getGlobalStats();

        long expectedUpdates =
                (long) numberOfThreads * updatesPerThread;

        assertEquals(
                expectedUpdates,
                result.inputTokens());

        assertEquals(
                expectedUpdates * 2,
                result.outputTokens());
    }
}