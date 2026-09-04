package com.hymin.webtoon_review.global.config;

import com.hymin.webtoon_review.global.annotation.Queue;
import com.hymin.webtoon_review.global.queue.JobQueue;
import com.hymin.webtoon_review.global.queue.JobQueue.JobQueueBuilder;
import com.hymin.webtoon_review.global.queue.QueueProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobQueueConfig {

    @Bean
    public JobQueue jobQueue(List<QueueProcessor<?>> queueProcessors) {
        JobQueueBuilder jobQueueBuilder = JobQueue.Builder();

        queueProcessors.forEach(queueProcessor -> {
            Class<?> realClass = AopProxyUtils.ultimateTargetClass(queueProcessor);
            Queue annotation = realClass.getAnnotation(Queue.class);
            if (annotation != null) {
                jobQueueBuilder.setTopic(
                    annotation.topic(),
                    annotation.retry(),
                    annotation.retryLimit(),
                    annotation.threshold(),
                    queueProcessor);
            }
        });

        return jobQueueBuilder.build();
    }
}
