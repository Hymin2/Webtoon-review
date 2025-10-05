package com.hymin.webtoon_review.global.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class JobQueue {

    private final Map<String, ArrayBlockingQueue<Job<?>>> jobs;
    private final Map<String, Class<? extends Exception>[]> retry;
    private final Map<String, Integer> retryLimit;
    private final Map<String, Integer> thresholds;
    private final Map<String, QueueProcessor<?>> processors;

    private JobQueue(
        Map<String, ArrayBlockingQueue<Job<?>>> jobs,
        Map<String, Class<? extends Exception>[]> retry,
        Map<String, Integer> retryLimit,
        Map<String, Integer> thresholds,
        Map<String, QueueProcessor<?>> processors
    ) {
        this.jobs = jobs;
        this.retry = retry;
        this.retryLimit = retryLimit;
        this.thresholds = thresholds;
        this.processors = processors;
    }

    public static JobQueueBuilder Builder() {
        return new JobQueueBuilder();
    }

    public List<String> getAllTopic() {
        return jobs.keySet()
            .stream()
            .toList();
    }

    public void add(String topic, Job<?> job) {
        getQueue(topic).add(job);
    }

    public void add(String topic, List<Job<?>> jobs) {
        jobs.forEach(job -> add(topic, job));
    }

    public void process(String topic) {
        QueueProcessor<?> processor = processors.get(topic);
        process(processors.get(topic), getJobList(jobs.get(topic), processor.getType()));
    }

    public Class<?> getType(String topic) {
        return processors.get(topic).getType();
    }

    public boolean isEmpty(String topic) {
        return getQueue(topic).isEmpty();
    }

    public boolean isGreaterThanThreshold(String topic) {
        return getQueue(topic).size() >= thresholds.get(topic);
    }

    public boolean isRetryable(String topic, Integer retryCount) {
        return retryLimit.get(topic) > retryCount;
    }

    public boolean isTopicExists(String topic) {
        return jobs.containsKey(topic);
    }

    public String toString(String topic) {
        return getQueue(topic).toString();
    }

    @SuppressWarnings("unchecked")
    private <T> List<Job<T>> getJobList(ArrayBlockingQueue<Job<?>> jobs, Class<?> clazz) {
        int size = jobs.size();
        List<Job<T>> jobList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Job<?> job = jobs.poll();

            if (job != null && clazz.isInstance(job.getData())) {
                jobList.add((Job<T>) job);
            }
        }

        return jobList;
    }

    private <T> void process(QueueProcessor<T> processor, List<Job<T>> jobList) {
        processor.process(jobList);
    }

    private ArrayBlockingQueue<Job<?>> getQueue(String topic) {
        if (!isTopicExists(topic)) {
            throw new IllegalArgumentException("Topic " + topic + " does not exist");
        }

        return jobs.get(topic);
    }

    public static class JobQueueBuilder {

        private final Map<String, ArrayBlockingQueue<Job<?>>> jobMap = new ConcurrentHashMap<>();
        private final Map<String, Class<? extends Exception>[]> retryMap = new HashMap<>();
        private final Map<String, Integer> retryLimitMap = new HashMap<>();
        private final Map<String, Integer> thresholdMap = new HashMap<>();
        private final Map<String, QueueProcessor<?>> processorMap = new HashMap<>();

        public JobQueueBuilder() {
        }

        public JobQueueBuilder setTopic(
            String topic,
            Class<? extends Exception>[] retry,
            Integer retryLimit,
            Integer threshold,
            QueueProcessor<?> processor) {
            if (jobMap.containsKey(topic)) {
                throw new IllegalArgumentException("Topic " + topic + " already exists");
            }

            int capacity = decideCapacity(threshold);

            jobMap.put(topic, new ArrayBlockingQueue<>(capacity));
            retryMap.put(topic, retry);
            retryLimitMap.put(topic, retryLimit);
            thresholdMap.put(topic, threshold);
            processorMap.put(topic, processor);

            return this;
        }

        public JobQueue build() {
            return new JobQueue(jobMap, retryMap, retryLimitMap, thresholdMap, processorMap);
        }

        private int decideCapacity(Integer threshold) {
            return Math.max(threshold * 2, 100);
        }
    }
}
