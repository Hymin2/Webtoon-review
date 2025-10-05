package com.hymin.webtoon_review.global.queue;

import java.util.List;

public interface QueueProcessor<T> {

    void process(List<Job<T>> jobs);

    Class<T> getType();
}
