package com.hymin.webtoon_review.chat.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
class ChatRecentMessageCacheServiceTest {

    private static final String ZSET_KEY = "chat:room:1:recent-messages";
    private static final String HASH_KEY = "chat:room:1:recent-message-contents";

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOperations;
    @Mock
    private HashOperations<String, String, String> hashOperations;

    private ObjectMapper objectMapper;
    private ChatRecentMessageCacheService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new ChatRecentMessageCacheService(objectMapper, redisTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void cachesMessageIdAndContentWithSequenceScore() {
        ChatMessageResponse message = response("message-2", 2L);

        service.cache(message);

        ArgumentCaptor<RedisScript<Long>> scriptCaptor = ArgumentCaptor.forClass(
            RedisScript.class
        );
        verify(redisTemplate).execute(
            scriptCaptor.capture(),
            eq(List.of(ZSET_KEY, HASH_KEY)),
            eq("message-2"),
            anyString(),
            eq("2"),
            eq("300"),
            eq("7200")
        );
        assertThat(scriptCaptor.getValue().getScriptAsString())
            .contains("ZADD", "HSET", "ZREMRANGEBYRANK", "HDEL", "EXPIRE");
    }

    @Test
    void cachesOnlyLatestThreeHundredMessagesFromDatabaseResult() {
        ChatRecentMessageCacheService cacheService = spy(service);
        List<ChatMessageResponse> messages = LongStream.rangeClosed(1, 301)
            .mapToObj(sequence -> response("message-" + sequence, sequence))
            .toList();
        doNothing().when(cacheService).cache(any(ChatMessageResponse.class));

        cacheService.cacheAll(messages);

        verify(cacheService, times(300)).cache(any(ChatMessageResponse.class));
        verify(cacheService, never()).cache(messages.get(0));
    }

    @Test
    void readsMessageIdsFromZSetAndContentsFromHash() throws Exception {
        ChatMessageResponse sequenceTwo = response("message-2", 2L);
        ChatMessageResponse sequenceThree = response("message-3", 3L);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.<String, String>opsForHash()).thenReturn(hashOperations);
        when(zSetOperations.rangeByScore(ZSET_KEY, 1D, Double.POSITIVE_INFINITY))
            .thenReturn(new LinkedHashSet<>(List.of("message-2", "message-3")));
        when(hashOperations.multiGet(HASH_KEY, List.of("message-2", "message-3")))
            .thenReturn(List.of(
                objectMapper.writeValueAsString(sequenceTwo),
                objectMapper.writeValueAsString(sequenceThree)
            ));

        List<ChatMessageResponse> result = service.getMessagesAfter(1L, 1L);

        assertThat(result)
            .extracting(ChatMessageResponse::getMessageId)
            .containsExactly("message-2", "message-3");
    }

    @Test
    void clearsInconsistentCacheWhenHashContentIsMissing() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.<String, String>opsForHash()).thenReturn(hashOperations);
        when(zSetOperations.rangeByScore(ZSET_KEY, 1D, Double.POSITIVE_INFINITY))
            .thenReturn(new LinkedHashSet<>(List.of("message-2", "message-3")));
        when(hashOperations.multiGet(HASH_KEY, List.of("message-2", "message-3")))
            .thenReturn(java.util.Arrays.asList("message-json", null));

        assertThat(service.getMessagesAfter(1L, 1L)).isEmpty();
        verify(redisTemplate).delete(List.of(ZSET_KEY, HASH_KEY));
    }

    private ChatMessageResponse response(String messageId, Long messageSequence) {
        return ChatMessageResponse.builder()
            .messageId(messageId)
            .roomId(1L)
            .messageSequence(messageSequence)
            .build();
    }
}
