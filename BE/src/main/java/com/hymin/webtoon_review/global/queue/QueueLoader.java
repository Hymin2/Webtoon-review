package com.hymin.webtoon_review.global.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueLoader {

    private final JobQueue jobQueue;
    private final QueueLogPathResolver queueLogPathResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void load() {
        Path topicPath = queueLogPathResolver.resolve(FileName.TOPIC_FILE_NAME);

        if (!Files.exists(topicPath)) {
            return;
        }

        try {
            List<String> topics = Files.readAllLines(topicPath);
            topics.forEach(topic -> {
                if (!jobQueue.isTopicExists(topic)) {
                    return;
                }

                Path dataPath = queueLogPathResolver.resolve(
                    FileName.QUEUE_DATE_FILE_NAME,
                    topic
                );

                if (!Files.exists(dataPath)) {
                    return;
                }

                try (Stream<String> lines = Files.lines(dataPath)) {
                    lines.forEach(data -> {
                        Job<?> job = deserializeJob(topic, data);
                        jobQueue.add(topic, job);
                    });

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Job<T> deserializeJob(String topic, String json) {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType javaType = typeFactory.constructParametricType(Job.class, jobQueue.getType(topic));

        try {
            return objectMapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
