package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.async.AsyncProcessor;
import com.hymin.webtoon_review.global.async.Job;
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
public class WebtoonViewAsyncService implements AsyncProcessor {

    private final WebtoonRepository webtoonRepository;

    @Async
    @Override
    @Transactional
    public void process(List<Job<?>> jobs) {
        webtoonRepository.updateViews(
            jobs
                .stream()
                .map(job -> (Long) job.getData())
                .toList()
        );
    }
}
