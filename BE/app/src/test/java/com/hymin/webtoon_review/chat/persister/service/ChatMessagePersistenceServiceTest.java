package com.hymin.webtoon_review.chat.persister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.metrics.ChatMessageMetrics;
import com.hymin.webtoon_review.global.constant.RedisGroupNames;
import com.hymin.webtoon_review.global.constant.RedisStreamKeys;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Map;
import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.BulkOperationException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.test.util.ReflectionTestUtils;

class ChatMessagePersistenceServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
    private final StreamOperations<String, String, String> streamOperations = mock(
        StreamOperations.class
    );
    private final BulkOperations bulkOperations = mock(BulkOperations.class);
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private ChatMessagePersistenceService service;

    @BeforeEach
    void setUp() {
        service = new ChatMessagePersistenceService(
            objectMapper,
            mongoTemplate,
            redisTemplate,
            new ChatMessageMetrics(meterRegistry)
        );
        ReflectionTestUtils.setField(service, "serverName", "persister-1");

        when(redisTemplate.<String, String>opsForStream()).thenReturn(streamOperations);
        when(mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ChatMessage.class))
            .thenReturn(bulkOperations);
        when(bulkOperations.insert(anyList())).thenReturn(bulkOperations);
    }

    @Test
    void acknowledgesDuplicatesAndRetriesOtherWriteErrors() throws Exception {
        List<MapRecord<String, String, String>> records = List.of(
            record("1-0", "message-1"),
            record("2-0", "message-2"),
            record("3-0", "message-3")
        );
        when(streamOperations.read(
            any(Consumer.class),
            any(StreamReadOptions.class),
            any(StreamOffset[].class)
        )).thenReturn(records, List.of());

        BulkWriteResult bulkWriteResult = mock(BulkWriteResult.class);
        when(bulkWriteResult.getInsertedCount()).thenReturn(1);
        BulkOperationException exception = mock(BulkOperationException.class);
        when(exception.getResult()).thenReturn(bulkWriteResult);
        when(exception.getErrors()).thenReturn(List.of(
            new BulkWriteError(11000, "duplicate", new BsonDocument(), 1),
            new BulkWriteError(121, "validation failure", new BsonDocument(), 2)
        ));
        when(bulkOperations.execute()).thenThrow(exception);

        service.processMessagesBatch();

        ArgumentCaptor<StreamReadOptions> readOptions = ArgumentCaptor.forClass(
            StreamReadOptions.class
        );
        verify(streamOperations).read(
            any(Consumer.class),
            readOptions.capture(),
            any(StreamOffset[].class)
        );
        assertThat(readOptions.getValue().getCount()).isEqualTo(1_000L);
        assertThat(readOptions.getValue().getBlock()).isEqualTo(1_000L);

        ArgumentCaptor<String[]> acknowledgedIds = ArgumentCaptor.forClass(String[].class);
        verify(streamOperations).acknowledge(
            eq(RedisStreamKeys.CHAT_MESSAGE_BATCH),
            eq(RedisGroupNames.CHAT_MESSAGE_BATCH),
            acknowledgedIds.capture()
        );
        assertThat(acknowledgedIds.getValue()).containsExactly("1-0", "2-0");
        assertThat(count("success")).isEqualTo(1);
        assertThat(count("duplicate")).isEqualTo(1);
        assertThat(count("failure")).isEqualTo(1);
    }

    private MapRecord<String, String, String> record(String recordId, String messageId)
        throws Exception {
        ChatMessage message = ChatMessage.builder().id(messageId).roomId(1L).build();
        return MapRecord.create(
            RedisStreamKeys.CHAT_MESSAGE_BATCH,
            Map.of("payload", objectMapper.writeValueAsString(message))
        ).withId(RecordId.of(recordId));
    }

    private double count(String result) {
        return meterRegistry.get("chat.messages")
            .tags("stage", "persisted", "result", result)
            .counter()
            .count();
    }
}
