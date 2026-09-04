package com.hymin.webtoon_review.global.queue;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueLogLockManager {

    private final static Integer DEFAULT_TIMEOUT_SECONDS = 30;

    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private final JobQueue jobQueue;

    @PostConstruct
    public void init() {
        jobQueue.getAllTopic().forEach(topic ->
            lockMap.put(topic, new ReentrantLock(true))
        );
    }

    public boolean acquireLock(String topic) {
        if (topic == null || topic.isEmpty() || !jobQueue.isTopicExists(topic)) {
            return false;
        }

        ReentrantLock lock = lockMap.get(topic);
        try {
            return lock.tryLock(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseLock(String topic) {
        if (topic == null || topic.isEmpty() || !jobQueue.isTopicExists(topic)) {
            return;
        }

        ReentrantLock lock = lockMap.get(topic);
        lock.unlock();
    }
}
