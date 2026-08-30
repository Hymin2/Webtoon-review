package com.hymin.chattest.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class CacheStampedeLoadClient implements AutoCloseable {

    private static final Duration READY_TIMEOUT = Duration.ofSeconds(30L);
    private static final Duration RESULT_TIMEOUT = Duration.ofSeconds(90L);

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10L))
        .version(HttpClient.Version.HTTP_1_1)
        .build();

    public Result execute(
        URI baseUrl,
        long roomId,
        long afterSequence,
        int requestCount
    ) {
        URI endpoint = URI.create(
            baseUrl + "/test/chat/room/" + roomId
                + "/messages?messageSequence=" + afterSequence
        );
        HttpRequest request = HttpRequest.newBuilder(endpoint)
            .timeout(Duration.ofSeconds(60L))
            .GET()
            .build();
        CountDownLatch readySignal = new CountDownLatch(requestCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<RequestResult> results = new ArrayList<>(requestCount);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<RequestResult>> futures = new ArrayList<>(requestCount);
            for (int index = 0; index < requestCount; index++) {
                futures.add(executor.submit(() -> executeRequest(
                    request,
                    readySignal,
                    startSignal
                )));
            }

            awaitAllRequestsReady(readySignal, startSignal);
            startSignal.countDown();
            for (Future<RequestResult> future : futures) {
                results.add(future.get(RESULT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("동시 요청 실행이 중단됐습니다.", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("동시 요청 결과를 확인하지 못했습니다.", exception);
        } finally {
            startSignal.countDown();
        }

        List<Long> latencies = new ArrayList<>(results.stream()
            .filter(RequestResult::success)
            .map(RequestResult::latencyMillis)
            .toList());
        Collections.sort(latencies);
        long successCount = results.stream().filter(RequestResult::success).count();
        Throwable firstFailure = results.stream()
            .filter(result -> !result.success())
            .map(RequestResult::failure)
            .filter(failure -> failure != null)
            .findFirst()
            .orElse(null);

        return new Result(
            requestCount,
            successCount,
            average(latencies),
            percentile(latencies, 0.95),
            percentile(latencies, 0.99),
            firstFailure
        );
    }

    private RequestResult executeRequest(
        HttpRequest request,
        CountDownLatch readySignal,
        CountDownLatch startSignal
    ) {
        readySignal.countDown();
        long startedAt = 0L;
        try {
            startSignal.await();
            startedAt = System.nanoTime();
            HttpResponse<Void> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.discarding()
            );
            boolean success = response.statusCode() == 200;
            Throwable failure = success
                ? null
                : new IllegalStateException("HTTP 상태 코드: " + response.statusCode());
            return new RequestResult(
                success,
                Duration.ofNanos(System.nanoTime() - startedAt).toMillis(),
                failure
            );
        } catch (IOException exception) {
            return failedRequest(startedAt, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return failedRequest(startedAt, exception);
        }
    }

    private void awaitAllRequestsReady(
        CountDownLatch readySignal,
        CountDownLatch startSignal
    ) throws InterruptedException {
        if (!readySignal.await(READY_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            startSignal.countDown();
            throw new IllegalStateException("모든 가상 스레드가 시작 지점에 도착하지 못했습니다.");
        }
    }

    private RequestResult failedRequest(long startedAt, Throwable failure) {
        long measuredFrom = startedAt == 0L ? System.nanoTime() : startedAt;
        return new RequestResult(
            false,
            Duration.ofNanos(System.nanoTime() - measuredFrom).toMillis(),
            failure
        );
    }

    private double average(List<Long> latencies) {
        return latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    private long percentile(List<Long> latencies, double percentile) {
        if (latencies.isEmpty()) {
            return 0L;
        }
        int index = Math.max(0, (int) Math.ceil(latencies.size() * percentile) - 1);
        return latencies.get(index);
    }

    @Override
    public void close() {
        httpClient.close();
    }

    public record Result(
        int requestCount,
        long successCount,
        double averageMillis,
        long p95Millis,
        long p99Millis,
        Throwable firstFailure
    ) {
    }

    private record RequestResult(boolean success, long latencyMillis, Throwable failure) {
    }
}
