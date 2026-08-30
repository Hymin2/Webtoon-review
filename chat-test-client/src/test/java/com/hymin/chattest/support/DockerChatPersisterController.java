package com.hymin.chattest.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class DockerChatPersisterController implements AutoCloseable {

    private static final Duration COMMAND_TIMEOUT = Duration.ofMinutes(3);
    private static final Duration START_TIMEOUT = Duration.ofMinutes(2);

    private final String containerName;

    public DockerChatPersisterController(
        PersistencePerformanceProperties properties,
        String instanceLabel,
        String persistenceMode,
        int batchSize,
        String streamKey,
        String groupName
    ) {
        this.containerName = "chat-persister-performance-" + instanceLabel + "-" + System.nanoTime();

        run(command(
            "docker", "run", "--detach", "--rm",
            "--name", containerName,
            "--network", properties.dockerNetwork(),
            "--env-file", properties.environmentFile().toString(),
            "-e", "SPRING_PROFILES_ACTIVE=chat-persister",
            "-e", "CHAT_INSTANCE_NAME=" + containerName,
            "-e", "CHAT_PERSISTENCE_MODE=" + persistenceMode,
            "-e", "CHAT_PERSISTENCE_BATCH_SIZE=" + batchSize,
            "-e", "CHAT_PERSISTENCE_STREAM_KEY=" + streamKey,
            "-e", "CHAT_PERSISTENCE_GROUP_NAME=" + groupName,
            "-e", "MYSQL_DATABASE_HOST=mysql",
            "-e", "MYSQL_DATABASE_PORT=3306",
            "-e", "REDIS_HOST=redis",
            "-e", "REDIS_PORT=6379",
            "-e", "MONGODB_HOST=mongodb",
            "-e", "MONGODB_PORT=27017",
            properties.dockerImage()
        ));
        awaitConsumerStart();
    }

    public static void verifyImage(PersistencePerformanceProperties properties) {
        CommandResult result = run(command(
            "docker", "image", "inspect", properties.dockerImage()
        ), false);
        if (result.exitCode() != 0) {
            throw new IllegalStateException(
                "성능 테스트용 백엔드 이미지가 없습니다: " + properties.dockerImage()
            );
        }
    }

    public void pause() {
        run(command("docker", "pause", containerName));
    }

    public void unpause() {
        run(command("docker", "unpause", containerName));
    }

    public BatchStatistics batchStatistics() {
        String logs = run(command("docker", "logs", containerName)).output();
        String marker = "채팅 메시지 저장 서버] 채팅 메시지 ";
        String suffix = "건 저장 시도";
        long batchCount = 0;
        long messageCount = 0;

        for (String line : logs.lines().toList()) {
            int markerIndex = line.indexOf(marker);
            int suffixIndex = line.indexOf(suffix, markerIndex + marker.length());
            if (markerIndex < 0 || suffixIndex < 0) {
                continue;
            }
            String count = line.substring(markerIndex + marker.length(), suffixIndex).trim();
            messageCount += Long.parseLong(count);
            batchCount++;
        }
        return new BatchStatistics(batchCount, messageCount);
    }

    private void awaitConsumerStart() {
        long deadline = System.nanoTime() + START_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            CommandResult result = run(command("docker", "logs", containerName), false);
            if (result.output().contains("Redis Stream 연속 소비 시작")) {
                return;
            }
            if (!isRunning()) {
                throw new IllegalStateException(
                    "채팅 메시지 저장 컨테이너가 시작되지 못했습니다.\n" + result.output()
                );
            }
            sleep(Duration.ofMillis(500));
        }
        throw new IllegalStateException("채팅 메시지 저장 컨테이너 시작 시간이 초과됐습니다.");
    }

    private boolean isRunning() {
        CommandResult result = run(command(
            "docker", "inspect", "--format", "{{.State.Running}}", containerName
        ), false);
        return result.exitCode() == 0 && result.output().trim().equals("true");
    }

    @Override
    public void close() {
        run(command("docker", "rm", "--force", containerName), false);
    }

    private static List<String> command(String... values) {
        return new ArrayList<>(List.of(values));
    }

    private static CommandResult run(List<String> command) {
        CommandResult result = run(command, true);
        if (result.exitCode() != 0) {
            throw new IllegalStateException(
                "Docker 명령 실행에 실패했습니다: " + String.join(" ", command)
                    + System.lineSeparator() + result.output()
            );
        }
        return result;
    }

    private static CommandResult run(List<String> command, boolean failOnTimeout) {
        try {
            Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
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
                if (failOnTimeout) {
                    throw new IllegalStateException(
                        "Docker 명령 실행 시간이 초과됐습니다: " + String.join(" ", command)
                    );
                }
                return new CommandResult(-1, "명령 실행 시간 초과");
            }
            String output = new String(
                outputFuture.get(10, TimeUnit.SECONDS),
                StandardCharsets.UTF_8
            );
            return new CommandResult(process.exitValue(), output);
        } catch (IOException exception) {
            throw new IllegalStateException("Docker 명령을 실행하지 못했습니다.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Docker 명령 실행 대기가 중단됐습니다.", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Docker 명령 출력을 읽지 못했습니다.", exception);
        }
    }

    private static void sleep(Duration duration) {
        try {
            TimeUnit.NANOSECONDS.sleep(duration.toNanos());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("컨테이너 시작 확인이 중단됐습니다.", exception);
        }
    }

    public record BatchStatistics(long batchCount, long messageCount) {

        public double averageBatchSize() {
            return batchCount == 0 ? 0.0 : (double) messageCount / batchCount;
        }
    }

    private record CommandResult(int exitCode, String output) {
    }
}
