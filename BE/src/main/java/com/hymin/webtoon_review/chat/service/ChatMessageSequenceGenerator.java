package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.global.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageSequenceGenerator {

    private final RedisTemplate<String, Long> redisTemplate;
    private final ChatMessageService chatMessageService;

    public Long generate(Long roomId) {
        String key = RedisKeys.CHAT_ROOM_PREFIX + roomId
            + RedisKeys.CHAT_ROOM_MESSAGE_SEQUENCE_POSTFIX;
        Long nextSeq = redisTemplate.opsForValue().increment(key);

        if (nextSeq == null || nextSeq == 1) {
            Long maxSeq = chatMessageService.getMaxMessageSequence(roomId);

            if (maxSeq > 0) {
                Boolean isSet = redisTemplate.opsForValue()
                    .setIfAbsent(key, maxSeq + 1);

                if (Boolean.TRUE.equals(isSet)) {
                    return maxSeq + 1;
                } else {
                    nextSeq = redisTemplate.opsForValue().increment(key);
                    return nextSeq;
                }
            }
        }

        return nextSeq;
    }
}
