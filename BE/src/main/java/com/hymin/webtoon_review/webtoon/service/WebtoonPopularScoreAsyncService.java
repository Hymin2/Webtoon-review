package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.annotation.Queue;
import com.hymin.webtoon_review.global.queue.Job;
import com.hymin.webtoon_review.global.queue.QueueProcessor;
import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
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
@Queue(topic = "popularity")
public class WebtoonPopularScoreAsyncService implements QueueProcessor<WebtoonPopularityScore> {

    private final WebtoonRepository webtoonRepository;

    @Async
    @Override
    @Transactional
    public void process(List<Job<WebtoonPopularityScore>> jobs) {
        webtoonRepository.updatePopularityScore(
            jobs
                .stream()
                .map(Job::getData)
                .toList()
        );
    }

    @Override
    public Class<WebtoonPopularityScore> getType() {
        return WebtoonPopularityScore.class;
    }
}
