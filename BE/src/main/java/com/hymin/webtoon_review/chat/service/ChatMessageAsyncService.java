package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.repository.ChatMessageRepository;
import com.hymin.webtoon_review.global.async.AsyncProcessor;
import com.hymin.webtoon_review.global.async.Job;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageAsyncService implements AsyncProcessor {

    private final ChatMessageRepository chatMessageRepository;

    @Async
    @Override
    public void process(List<Job<?>> jobs) {
        chatMessageRepository.saveAll(
            jobs
                .stream()
                .map(job -> (ChatMessage) job.getData())
                .toList()
        );
    }
}
