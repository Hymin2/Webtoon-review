package com.hymin.webtoon_review.global.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JobQueue {

    private final Map<String, ConcurrentLinkedQueue<Job<?>>> jobs;
    private final Map<String, Integer> thresholds;
    private final Map<String, AsyncProcessor> processors;

    private JobQueue(Map<String, ConcurrentLinkedQueue<Job<?>>> jobs,
        Map<String, Integer> thresholds, Map<String, AsyncProcessor> processors) {
        this.jobs = jobs;
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

    public void processAll(String topic) {
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

    private ConcurrentLinkedQueue<Job<?>> getQueue(String topic) {
        if (!jobs.containsKey(topic)) {
            throw new IllegalArgumentException("Topic " + topic + " does not exist");
        }

        return jobs.get(topic);
    }

    public static class JobQueueBuilder {

        private final Map<String, ConcurrentLinkedQueue<Job<?>>> jobs = new ConcurrentHashMap<>();
        private final Map<String, Integer> thresholds = new ConcurrentHashMap<>();
        private final Map<String, AsyncProcessor> processors = new ConcurrentHashMap<>();

        public JobQueueBuilder() {
        }

        public JobQueueBuilder setTopic(String topic, Integer threshold, AsyncProcessor processor) {
            jobs.put(topic, new ConcurrentLinkedQueue<>());
            thresholds.put(topic, threshold);
            processors.put(topic, processor);

            return this;
        }

        public JobQueue build() {
            return new JobQueue(jobs, thresholds, processors);
        }
    }
}
