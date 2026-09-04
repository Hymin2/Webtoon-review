package com.hymin.webtoon_review.chat.persister.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.metrics.ChatMessageMetrics;
import com.hymin.webtoon_review.global.constant.RedisGroupNames;
import com.hymin.webtoon_review.global.constant.RedisStreamKeys;
import com.mongodb.ErrorCategory;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.BulkOperationException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("chat-persister")
@RequiredArgsConstructor
public class ChatMessagePersistenceService {

    private static final long MESSAGE_BATCH_SIZE = 1_000L;

    @Value("${server.instance.name:default}")
    private String serverName;

    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ChatMessageMetrics chatMessageMetrics;

    public void processMessagesBatch() {
        String groupName = RedisGroupNames.CHAT_MESSAGE_BATCH;
        String consumerName = serverName;
        String streamKey = RedisStreamKeys.CHAT_MESSAGE_BATCH;

        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();

        List<MapRecord<String, String, String>> records = streamOps.read(
            Consumer.from(groupName, consumerName),
            StreamReadOptions.empty().count(MESSAGE_BATCH_SIZE).block(Duration.ofSeconds(1)),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed())
        );

        if (records == null || records.isEmpty()) {
            return;
        }

        log.info("[채팅 메시지 저장 서버] 채팅 메시지 {}건 저장 시도", records.size());
        BatchSaveResult result = insertBatch(records);
        acknowledgeMessage(records, result.retryableErrorIndexes());
        log.info(
            "[채팅 메시지 저장 서버] 채팅 메시지 {}건 저장 완료, {}건 중복, {}건 에러 발생",
            records.size(),
            result.duplicateIndexes().size(),
            result.retryableErrorIndexes().size()
        );
    }

    @Async("messagesBatchExecutor")
    public void processPendingMessagesBatch() {
        String groupName = RedisGroupNames.CHAT_MESSAGE_BATCH;
        String consumerName = "chat-message-batch-recovery-" + serverName;
        String streamKey = RedisStreamKeys.CHAT_MESSAGE_BATCH;

        PendingMessages pendingMessages = redisTemplate.opsForStream()
            .pending(streamKey, groupName, Range.unbounded(), 100L);

        if (pendingMessages.isEmpty()) {
            return;
        }

        List<RecordId> pendingMessageIds = new ArrayList<>();
        List<RecordId> deadLettersIds = new ArrayList<>();

        for (PendingMessage pm : pendingMessages) {
            if (pm.getTotalDeliveryCount() >= 5) {
                deadLettersIds.add(pm.getId());
            }

            if (pm.getElapsedTimeSinceLastDelivery().getSeconds() > 20) {
                pendingMessageIds.add(pm.getId());
            }
        }

        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();

        if (!deadLettersIds.isEmpty()) {
            List<MapRecord<String, String, String>> records = streamOps.claim(
                streamKey,
                groupName,
                consumerName,
                Duration.ofSeconds(1L),
                deadLettersIds.toArray(deadLettersIds.toArray(new RecordId[0]))
            );

            acknowledgeMessage(records);
            // moveToDeadLetterQueue(records);
        }

        if (!pendingMessageIds.isEmpty()) {
            List<MapRecord<String, String, String>> records = streamOps.claim(
                streamKey,
                groupName,
                consumerName,
                Duration.ofSeconds(20),
                pendingMessageIds.toArray(new RecordId[0])
            );

            BatchSaveResult result = insertBatch(records);
            acknowledgeMessage(records, result.retryableErrorIndexes());
        }
    }

    private BatchSaveResult insertBatch(List<MapRecord<String, String, String>> records) {
        try {
            BatchSaveResult result = saveBatch(
                records.stream().map(this::parseMessage).toList()
            );
            chatMessageMetrics.persisted(
                result.insertedCount(),
                result.duplicateIndexes().size(),
                result.retryableErrorIndexes().size()
            );
            return result;
        } catch (RuntimeException e) {
            chatMessageMetrics.persisted(0, 0, records.size());
            throw e;
        }
    }

    private void acknowledgeMessage(
        List<MapRecord<String, String, String>> records,
        List<Integer> errorIndexes
    ) {
        String groupName = RedisGroupNames.CHAT_MESSAGE_BATCH;
        String streamKey = RedisStreamKeys.CHAT_MESSAGE_BATCH;
        List<String> successIds = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            if (errorIndexes.contains(i)) {
                continue;
            }

            successIds.add(records.get(i).getId().getValue());
        }

        redisTemplate.opsForStream()
            .acknowledge(streamKey, groupName, successIds.toArray(new String[0]));
    }

    private void acknowledgeMessage(
        List<MapRecord<String, String, String>> records
    ) {
        String groupName = RedisGroupNames.CHAT_MESSAGE_BATCH;
        String streamKey = RedisStreamKeys.CHAT_MESSAGE_BATCH;
        List<String> ids = records.stream().map((record) -> record.getId().getValue()).toList();

        redisTemplate.opsForStream()
            .acknowledge(streamKey, groupName, ids.toArray(new String[0]));
    }

    private ChatMessage parseMessage(MapRecord<String, String, String> record) {
        try {
            return objectMapper.readValue(record.getValue().get("payload"), ChatMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private BatchSaveResult saveBatch(List<ChatMessage> messages) {
        List<Integer> duplicateIndexes = new ArrayList<>();
        List<Integer> retryableErrorIndexes = new ArrayList<>();

        try {
            BulkWriteResult result = mongoTemplate.bulkOps(
                    BulkOperations.BulkMode.UNORDERED,
                    ChatMessage.class)
                .insert(messages)
                .execute();

            return new BatchSaveResult(
                result.getInsertedCount(),
                duplicateIndexes,
                retryableErrorIndexes
            );
        } catch (BulkOperationException e) {
            for (BulkWriteError error : e.getErrors()) {
                if (ErrorCategory.fromErrorCode(error.getCode()) == ErrorCategory.DUPLICATE_KEY) {
                    duplicateIndexes.add(error.getIndex());
                    continue;
                }

                retryableErrorIndexes.add(error.getIndex());
            }

            return new BatchSaveResult(
                e.getResult().getInsertedCount(),
                duplicateIndexes,
                retryableErrorIndexes
            );
        }
    }

    private record BatchSaveResult(
        int insertedCount,
        List<Integer> duplicateIndexes,
        List<Integer> retryableErrorIndexes
    ) {
    }
}
