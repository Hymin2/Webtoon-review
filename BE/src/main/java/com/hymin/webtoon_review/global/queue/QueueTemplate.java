package com.hymin.webtoon_review.global.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueTemplate {

    private final JobQueue jobQueue;
    private final QueueLogWriter queueLogWriter;

    public void add(String topic, Job<?> job) {
        jobQueue.add(topic, job);
        queueLogWriter.appendLog(topic, job);
    }
}
