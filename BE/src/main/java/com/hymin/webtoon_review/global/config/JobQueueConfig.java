package com.hymin.webtoon_review.global.config;

import com.hymin.webtoon_review.global.async.JobQueue;
import com.hymin.webtoon_review.global.async.TopicNames;
import com.hymin.webtoon_review.webtoon.service.WebtoonPopularScoreAsyncService;
import com.hymin.webtoon_review.webtoon.service.WebtoonViewAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobQueueConfig {

    private final WebtoonViewAsyncService webtoonViewAsyncService;
    private final WebtoonPopularScoreAsyncService webtoonPopularScoreAsyncService;

    @Bean
    public JobQueue jobQueue() {
        JobQueue jobQueue = new JobQueue();
        jobQueue.setTopic(TopicNames.view.name(), 100, webtoonViewAsyncService);
        jobQueue.setTopic(TopicNames.popularity.name(), 100, webtoonPopularScoreAsyncService);

        return jobQueue;
    }
}
