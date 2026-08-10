package com.hymin.webtoon_review.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.dto.ChatMessageDto;
import com.hymin.webtoon_review.chat.metrics.ChatMessageMetrics;
import com.hymin.webtoon_review.chat.route.ChatWorkerLocalHashRing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("chat")
@RequiredArgsConstructor
public class ChatMessageRoutingService {

    private final ObjectMapper objectMapper;
    private final ChatWorkerLocalHashRing chatWorkerLocalHashRing;
    private final RedisTemplate<String, String> redisTemplate;
    private final ChatMessageMetrics chatMessageMetrics;

    public void route(ChatMessageDto chatMessageDto) {
        try {
            ObjectRecord<String, String> record = StreamRecords.newRecord()
                .in(chatWorkerLocalHashRing.getTargetServerStreamKey(chatMessageDto.getRoomId()))
                .ofObject(objectMapper.writeValueAsString(chatMessageDto));

            redisTemplate.opsForStream().add(record);
            chatMessageMetrics.receivedSuccess();
            log.info("[채팅] 채팅 메시지를 Redis Stream에 발행, {}",
                chatMessageDto.getClientMessageId());
        } catch (Exception e) {
            chatMessageMetrics.receivedFailure();
            throw new RuntimeException(e);
        }
    }
}
