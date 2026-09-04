package com.hymin.webtoon_review.global.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueLogWriter {

    private final JobQueue jobQueue;
    private final QueueLogLockManager queueLogLockManager;
    private final QueueLogPathResolver queueLogPathResolver;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, StringBuffer> logBuffer = new ConcurrentHashMap<>();
    private final Map<String, StringBuffer> errorLogBuffer = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(queueLogPathResolver.rootDirectory());

            saveTopicName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendLog(String topic, Job<?> job) {
        appendToBuffer(logBuffer, topic, job);
    }

    public void appendErrorLog(String topic, Job<?> job) {
        appendToBuffer(errorLogBuffer, topic, job);
    }

    private void appendToBuffer(Map<String, StringBuffer> buffer, String topic, Job<?> job) {
        try {
            String data = mapper.writeValueAsString(job);

            buffer.
                computeIfAbsent(topic, k -> new StringBuffer())
                .append(data)
                .append("\n");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveTopicName() {
        StringBuilder data = new StringBuilder();

        jobQueue.getAllTopic().forEach(topic -> {
            logBuffer.put(topic, new StringBuffer());
            errorLogBuffer.put(topic, new StringBuffer());
            data.append(topic).append("\n");
        });

        save(queueLogPathResolver.resolve(FileName.TOPIC_FILE_NAME),
            data.toString(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    @Scheduled(fixedRate = 1000)
    private void saveLog() {
        jobQueue.getAllTopic().forEach(topic -> {
            if (queueLogLockManager.acquireLock(topic)) {
                save(queueLogPathResolver.resolve(FileName.QUEUE_DATE_FILE_NAME, topic),
                    logBuffer.get(topic).toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
                );
                logBuffer.get(topic).setLength(0);

                save(queueLogPathResolver.resolve(FileName.ERROR_LOG_FILE_NAME, topic),
                    errorLogBuffer.get(topic).toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

                queueLogLockManager.releaseLock(topic);
            }
        });
    }

    private void save(Path path, String data, OpenOption... options) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, data.getBytes(), options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
