package com.hymin.webtoon_review.global.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobQueueMonitorService {

    private final JobQueue jobQueue;

    @Scheduled(fixedRate = 1000)
    public void monitor() {
        jobQueue.getAllTopic()
            .forEach(topic -> {
                if (jobQueue.isGreaterThanThreshold(topic)) {
                    jobQueue.process(topic);
                }
            });
    }

    @Scheduled(fixedRate = 1000)
    public void retry() {
        jobQueue.getAllTopic().forEach(jobQueue::retry);
    }
}
