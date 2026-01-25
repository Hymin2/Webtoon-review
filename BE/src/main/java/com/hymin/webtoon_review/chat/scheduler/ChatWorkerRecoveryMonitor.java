package com.hymin.webtoon_review.chat.scheduler;

import com.hymin.webtoon_review.chat.service.ChatWorkerRecoverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWorkerRecoveryMonitor {

    private final ChatWorkerRecoverService chatWorkerRecoverService;

    @Scheduled(fixedDelay = 5000)
    public void monitor() {
        chatWorkerRecoverService.runRecovery();
    }
}
