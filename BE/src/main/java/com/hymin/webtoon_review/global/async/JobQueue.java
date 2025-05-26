package com.hymin.webtoon_review.global.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class JobQueue {

    private final Map<String, ArrayBlockingQueue<Job<?>>> jobs;
    private final Map<String, ArrayBlockingQueue<Job<?>>> retries;
    private final Map<String, Integer> thresholds;
    private final Map<String, AsyncProcessor> processors;

    private JobQueue(
        Map<String, ArrayBlockingQueue<Job<?>>> jobs,
        Map<String, ArrayBlockingQueue<Job<?>>> retries,
        Map<String, Integer> thresholds, Map<String, AsyncProcessor> processors
    ) {
        this.jobs = jobs;
        this.retries = retries;
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

    public void addRetry(String topic, Job<?> job) {
        getRetries(topic).add(job);
    }

    public void addRetry(String topic, List<Job<?>> jobs) {
        jobs.forEach(job -> addRetry(topic, job));
    }

    public void retry(String topic) {
        int size = getQueue(topic).size();
        List<Job<?>> jobList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            jobList.add(getRetries(topic).poll());
        }

        processors.get(topic).process(jobList);
    }

    public void process(String topic) {
        int size = getQueue(topic).size();
        List<Job<?>> jobList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            jobList.add(getQueue(topic).poll());
        }

        processors.get(topic).process(jobList);
    }

    public boolean isEmpty(String topic) {
        return getQueue(topic).isEmpty();
    }

    public boolean isGreaterThanThreshold(String topic) {
        return getQueue(topic).size() >= thresholds.get(topic);
    }

    private ArrayBlockingQueue<Job<?>> getQueue(String topic) {
        if (!jobs.containsKey(topic)) {
            throw new IllegalArgumentException("Topic " + topic + " does not exist");
        }

        return jobs.get(topic);
    }

    private ArrayBlockingQueue<Job<?>> getRetries(String topic) {
        if (!retries.containsKey(topic)) {
            throw new IllegalArgumentException("Topic " + topic + " does not exist");
        }

        return retries.get(topic);
    }

    public static class JobQueueBuilder {

        private final Map<String, ArrayBlockingQueue<Job<?>>> jobs = new ConcurrentHashMap<>();
        private final Map<String, ArrayBlockingQueue<Job<?>>> retries = new ConcurrentHashMap<>();
        private final Map<String, Integer> thresholds = new ConcurrentHashMap<>();
        private final Map<String, AsyncProcessor> processors = new ConcurrentHashMap<>();

        public JobQueueBuilder() {
        }

        public JobQueueBuilder setTopic(String topic, Integer threshold, AsyncProcessor processor) {
            if (jobs.containsKey(topic)) {
                throw new IllegalArgumentException("Topic " + topic + " already exists");
            }

            jobs.put(topic, new ArrayBlockingQueue<>(10000));
            retries.put(topic, new ArrayBlockingQueue<>(10000));
            thresholds.put(topic, threshold);
            processors.put(topic, processor);

            return this;
        }

        public JobQueue build() {
            return new JobQueue(jobs, retries, thresholds, processors);
        }
    }
}
