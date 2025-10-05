package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.annotation.Queue;
import com.hymin.webtoon_review.global.queue.Job;
import com.hymin.webtoon_review.global.queue.QueueProcessor;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Queue(topic = "view", threshold = 1)
public class WebtoonViewAsyncService implements QueueProcessor<Long> {

    private final WebtoonRepository webtoonRepository;

    @Async
    @Override
    @Transactional
    public void process(List<Job<Long>> jobs) {
        webtoonRepository.updateViews(
            jobs
                .stream()
                .map(Job::getData)
                .toList()
        );
    }

    @Override
    public Class<Long> getType() {
        return Long.class;
    }
}
