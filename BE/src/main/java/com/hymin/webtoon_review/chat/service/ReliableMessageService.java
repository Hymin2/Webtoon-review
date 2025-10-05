package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.dto.ChatRequest.RetryMessage;
import com.hymin.webtoon_review.chat.repository.UnacknowledgedMessageRepository;
import com.hymin.webtoon_review.global.queue.Job;
import com.hymin.webtoon_review.global.queue.JobQueue;
import com.hymin.webtoon_review.user.entity.User;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReliableMessageService {

    private static final Integer MAX_RETRY_COUNT = 5;
    private static final Integer RETRY_TIME_LIMIT_MILLIS = 1000;
    private static final String RETRY_DESTINATION = "/retry";

    private final JobQueue jobQueue;
    private final MessageService messageService;
    private final UnacknowledgedMessageRepository unacknowledgedMessageRepository;

    public void addRetryMessage(List<User> connectedUser, ChatMessage chatMessage) {
        connectedUser.forEach(user -> {
            if (isNotSender(user, chatMessage)) {
                unacknowledgedMessageRepository.addRetryMessage(user.getUsername(), chatMessage);
            }
        });
    }

    public void removeRetryMessage(String username, String messageUUID) {
        unacknowledgedMessageRepository.removeRetryMessage(username, messageUUID);
    }

    @Scheduled(fixedRate = 500)
    public void retryUnacknowledgedMessages() {
        unacknowledgedMessageRepository.getRetryMessages().forEach((username, retryMessages) -> {
            retryMessages.forEach((messageUUID, retryMessage) -> {
                if (isWithinRetryTimeLimit(retryMessage)) {
                    return;
                }

                if (retryMessage.getRetryCount() >= MAX_RETRY_COUNT) {
                    removeRetryMessage(username, retryMessage.getMessageUUID());
                    jobQueue.add("unacknowledgedMessage", Job.of(retryMessage));
                }

                messageService.send(username, RETRY_DESTINATION, ChatMessage.from(retryMessage));
                retryMessage.increaseRetryCount();
                retryMessage.updateLastAttemptTime();
            });
        });
    }

    private boolean isWithinRetryTimeLimit(RetryMessage retryMessage) {
        Duration duration = Duration.between(LocalDateTime.now(),
            retryMessage.getLastAttemptTime());

        return duration.toMillis() <= RETRY_TIME_LIMIT_MILLIS;
    }

    private boolean isNotSender(User user, ChatMessage chatMessage) {
        return !chatMessage.getSender().equals(user.getNickname());
    }
}
