package com.hymin.webtoon_review.global.config;

import com.hymin.webtoon_review.chat.service.ChatMessageAsyncService;
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

    private final ChatMessageAsyncService chatMessageAsyncService;
    private final WebtoonViewAsyncService webtoonViewAsyncService;
    private final WebtoonPopularScoreAsyncService webtoonPopularScoreAsyncService;

    @Bean
    public JobQueue jobQueue() {
        return JobQueue.Builder()
            .setTopic(TopicNames.view.name(), 100, webtoonViewAsyncService)
            .setTopic(TopicNames.popularity.name(), 100, webtoonPopularScoreAsyncService)
            .setTopic(TopicNames.chat.name(), 100, chatMessageAsyncService)
            .build();
    }
}
