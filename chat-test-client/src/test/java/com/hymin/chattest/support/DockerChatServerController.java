package com.hymin.chattest.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class DockerChatServerController {

    private static final Duration COMMAND_TIMEOUT = Duration.ofMinutes(5L);
    private static final Duration START_TIMEOUT = Duration.ofMinutes(3L);
    private static final List<URI> CHAT_SERVER_HEALTH_ENDPOINTS = List.of(
        URI.create("http://localhost:18081/actuator/health"),
        URI.create("http://localhost:18082/actuator/health")
    );

    private final CacheStampedeTestProperties properties;
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2L))
        .build();

    public DockerChatServerController(CacheStampedeTestProperties properties) {
        this.properties = properties;
    }

    public void start(
        boolean distributedLockEnabled,
        int loadBalancerPort,
        Duration databaseLoadDelay
    ) {
        verifyImage();
        List<String> command = new ArrayList<>(List.of(
            "docker", "compose",
            "--env-file", properties.environmentFile().toString(),
            "--file", properties.backendDirectory().resolve("compose.yml").toString(),
            "up", "--detach", "--force-recreate", "--no-build",
            "chat-1", "chat-2", "chat-lb"
        ));
        run(command, distributedLockEnabled, loadBalancerPort, databaseLoadDelay);
        awaitHealthy(loadBalancerPort);
    }

    private void verifyImage() {
        CommandResult result = run(
            List.of("docker", "image", "inspect", properties.dockerImage()),
            true,
            null,
            null,
            null
        );
        if (result.exitCode() != 0) {
            throw new IllegalStateException(
                "캐시 스탬피드 테스트용 백엔드 이미지가 없습니다: " + properties.dockerImage()
            );
        }
    }

    private void awaitHealthy(int loadBalancerPort) {
        long deadline = System.nanoTime() + START_TIMEOUT.toNanos();
        URI loadBalancerHealth = URI.create(
            "http://localhost:" + loadBalancerPort + "/lb-health"
        );
        while (System.nanoTime() < deadline) {
            if (CHAT_SERVER_HEALTH_ENDPOINTS.stream().allMatch(this::isHealthy)
                && isHealthy(loadBalancerHealth)) {
                return;
            }
            sleep(Duration.ofSeconds(1L));
        }
        throw new IllegalStateException("채팅 서버 또는 LB의 시작 시간이 초과됐습니다.");
    }

    private boolean isHealthy(URI endpoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(2L))
                .GET()
                .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.discarding())
                .statusCode() == 200;
        } catch (IOException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("채팅 서버 상태 확인이 중단됐습니다.", exception);
        }
    }

    private void run(
        List<String> command,
        boolean distributedLockEnabled,
        int loadBalancerPort,
        Duration databaseLoadDelay
    ) {
        CommandResult result = run(
            command,
            false,
            distributedLockEnabled,
            loadBalancerPort,
            databaseLoadDelay
        );
        if (result.exitCode() != 0) {
            throw new IllegalStateException(
                "채팅 서버 실행에 실패했습니다."
                    + System.lineSeparator() + result.output()
            );
        }
    }

    private CommandResult run(
        List<String> command,
        boolean ignoreFailure,
        Boolean distributedLockEnabled,
        Integer loadBalancerPort,
        Duration databaseLoadDelay
    ) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                .redirectErrorStream(true);
            if (distributedLockEnabled != null) {
                processBuilder.environment().put(
                    "CHAT_MESSAGE_QUERY_DISTRIBUTED_LOCK_ENABLED",
                    distributedLockEnabled.toString()
                );
            }
            if (loadBalancerPort != null) {
                processBuilder.environment().put("CHAT_PORT", loadBalancerPort.toString());
            }
            if (databaseLoadDelay != null) {
                processBuilder.environment().put(
                    "CHAT_MESSAGE_QUERY_DATABASE_LOAD_DELAY",
                    databaseLoadDelay.toMillis() + "ms"
                );
                processBuilder.environment().put(
                    "CHAT_SPRING_PROFILES_ACTIVE",
                    databaseLoadDelay.isZero()
                        ? "chat"
                        : "chat,chat-cache-stampede-test"
                );
            }
            Process process = processBuilder.start();
            CompletableFuture<byte[]> outputFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return process.getInputStream().readAllBytes();
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            });
            boolean completed = process.waitFor(COMMAND_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new IllegalStateException("Docker 명령 실행 시간이 초과됐습니다.");
            }
            String output = new String(
                outputFuture.get(10L, TimeUnit.SECONDS),
                StandardCharsets.UTF_8
            );
            CommandResult result = new CommandResult(process.exitValue(), output);
            if (!ignoreFailure && result.exitCode() != 0) {
                return result;
            }
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("Docker 명령을 실행하지 못했습니다.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Docker 명령 실행 대기가 중단됐습니다.", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Docker 명령 출력을 읽지 못했습니다.", exception);
        }
    }

    private void sleep(Duration duration) {
        try {
            TimeUnit.NANOSECONDS.sleep(duration.toNanos());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("채팅 서버 시작 대기가 중단됐습니다.", exception);
        }
    }

    private record CommandResult(int exitCode, String output) {
    }
}
