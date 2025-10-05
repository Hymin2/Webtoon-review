package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.chat.dto.ChatRequest.RetryMessage;
import com.hymin.webtoon_review.chat.repository.ChatMessageRepository;
import com.hymin.webtoon_review.global.annotation.Queue;
import com.hymin.webtoon_review.global.queue.Job;
import com.hymin.webtoon_review.global.queue.QueueProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Queue(topic = "unacknowledged_message", threshold = 100)
public class UnacknowledgedMessageAsyncService implements QueueProcessor<RetryMessage> {

    private final ChatMessageRepository chatMessageRepository;

    @Async
    @Override
    public void process(List<Job<RetryMessage>> jobs) {
        chatMessageRepository.saveUnacknowledgedMessages(
            jobs.stream()
                .map(Job::getData)
                .toList()
        );
    }

    @Override
    public Class<RetryMessage> getType() {
        return RetryMessage.class;
    }
}
