package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.async.AsyncProcessor;
import com.hymin.webtoon_review.global.async.Job;
import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebtoonPopularScoreAsyncService implements AsyncProcessor {

    private final WebtoonRepository webtoonRepository;

    @Async
    @Override
    @Transactional
    public void process(List<Job<?>> jobs) {
        webtoonRepository.updatePopularityScore(
            jobs
                .stream()
                .map(job -> (WebtoonPopularityScore) job.getData())
                .toList()
        );
    }
}
