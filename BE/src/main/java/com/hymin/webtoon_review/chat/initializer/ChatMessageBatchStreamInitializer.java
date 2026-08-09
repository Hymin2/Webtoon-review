package com.hymin.webtoon_review.chat.initializer;

import com.hymin.webtoon_review.global.constant.RedisGroupNames;
import com.hymin.webtoon_review.global.constant.RedisStreamKeys;
import com.hymin.webtoon_review.global.manager.RedisStreamGroupManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("chat-persister")
@RequiredArgsConstructor
public class ChatMessageBatchStreamInitializer {

    private final RedisStreamGroupManager redisStreamGroupManager;

    @PostConstruct
    public void initialize() {
        redisStreamGroupManager.createStreamAndGroup(
            RedisStreamKeys.CHAT_MESSAGE_BATCH,
            RedisGroupNames.CHAT_MESSAGE_BATCH
        );
        log.info("[채팅 메시지 저장 서버] Redis Stream과 Consumer Group 초기화 완료");
    }
}
