package com.hymin.webtoon_review.chat.server.service;

import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.entity.MessageCreatedOutbox;
import com.hymin.webtoon_review.chat.common.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.common.repository.UserChatRoomRepository;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageCommand;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageResult;
import com.hymin.webtoon_review.chat.server.exception.ChatMessageCommitUncertainException;
import com.hymin.webtoon_review.chat.server.metrics.ChatMessageCommandMetrics;
import com.hymin.webtoon_review.chat.server.repository.ChatMessageCommandRepository;
import com.hymin.webtoon_review.global.config.MongoTransactionConfiguration;
import com.mongodb.MongoException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
public class CreateChatMessageCommandService {

    private static final String TRANSIENT_TRANSACTION_ERROR = "TransientTransactionError";
    private static final String UNKNOWN_COMMIT_RESULT = "UnknownTransactionCommitResult";
    private static final int DURABLE_RESULT_LOOKUP_ATTEMPTS = 5;
    private static final long DURABLE_RESULT_LOOKUP_DELAY_MILLIS = 10L;
    private static final DateTimeFormatter CREATED_AT_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss.SSS"
    ).withZone(ZoneOffset.UTC);

    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageCommandRepository commandRepository;
    private final TransactionOperations transactionOperations;
    private final ChatMessageCommandMetrics metrics;
    private final int maxTransactionAttempts;
    private final Clock clock;

    public CreateChatMessageCommandService(
        UserChatRoomRepository userChatRoomRepository,
        ChatMessageCommandRepository commandRepository,
        ChatMessageCommandMetrics metrics,
        @Qualifier(MongoTransactionConfiguration.TRANSACTION_MANAGER)
        MongoTransactionManager transactionManager,
        @Value("${chat.message.command.transaction.max-attempts:5}") int maxTransactionAttempts
    ) {
        this(
            userChatRoomRepository,
            commandRepository,
            metrics,
            transactionTemplate(transactionManager),
            maxTransactionAttempts,
            Clock.systemUTC()
        );
    }

    CreateChatMessageCommandService(
        UserChatRoomRepository userChatRoomRepository,
        ChatMessageCommandRepository commandRepository,
        ChatMessageCommandMetrics metrics,
        TransactionOperations transactionOperations,
        int maxTransactionAttempts,
        Clock clock
    ) {
        if (maxTransactionAttempts < 1) {
            throw new IllegalArgumentException("maxTransactionAttempts must be positive");
        }
        this.userChatRoomRepository = userChatRoomRepository;
        this.commandRepository = commandRepository;
        this.transactionOperations = transactionOperations;
        this.metrics = metrics;
        this.maxTransactionAttempts = maxTransactionAttempts;
        this.clock = clock;
    }

    public CreateChatMessageResult create(CreateChatMessageCommand command) {
        String roomMemberId = userChatRoomRepository.findRoomMemberId(
                command.senderId(),
                command.roomId()
            )
            .orElseThrow(InvalidChatRoomAccessException::new);
        Optional<ChatMessage> existing = findDurableResult(command);
        if (existing.isPresent()) {
            return new CreateChatMessageResult(false, existing.get());
        }

        String messageId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        String createdAt = CREATED_AT_FORMATTER.format(Instant.now(clock));

        for (int attempt = 1; attempt <= maxTransactionAttempts; attempt++) {
            try {
                return transactionOperations.execute(status -> createInTransaction(
                    command,
                    roomMemberId,
                    messageId,
                    eventId,
                    createdAt
                ));
            } catch (RuntimeException exception) {
                if (hasErrorLabel(exception, UNKNOWN_COMMIT_RESULT)) {
                    return resolveCommittedResultOrThrow(command, messageId, exception);
                }
                if (isDuplicateKey(exception)) {
                    return resolveCommittedResultOrThrow(command, null, exception);
                }
                if (hasErrorLabel(exception, TRANSIENT_TRANSACTION_ERROR)) {
                    if (attempt < maxTransactionAttempts) {
                        metrics.transactionRetry();
                        log.warn(
                            "Retrying transient chat message transaction: roomId={}, attempt={}",
                            command.roomId(),
                            attempt + 1
                        );
                        continue;
                    }
                    metrics.transactionRetryExhausted();
                    log.error(
                        "Chat message transaction retries exhausted: roomId={}, attempts={}",
                        command.roomId(),
                        maxTransactionAttempts
                    );
                }
                throw exception;
            }
        }
        throw new IllegalStateException("Unreachable transaction retry state");
    }

    private CreateChatMessageResult createInTransaction(
        CreateChatMessageCommand command,
        String roomMemberId,
        String messageId,
        String eventId,
        String createdAt
    ) {
        Optional<ChatMessage> existing = findDurableResult(command);
        if (existing.isPresent()) {
            return new CreateChatMessageResult(false, existing.get());
        }

        long roomSequence = commandRepository.allocateNextRoomSequence(command.roomId());
        ChatMessage message = ChatMessage.builder()
            .id(messageId)
            .roomId(command.roomId())
            .senderId(command.senderId())
            .roomMemberId(roomMemberId)
            .messageSequence(roomSequence)
            .clientMessageId(command.clientMessageId())
            .sender(command.sender())
            .createdAt(createdAt)
            .messageBlocks(command.messageBlocks())
            .build();
        commandRepository.insertMessage(message);
        commandRepository.insertOutbox(MessageCreatedOutbox.builder()
            .eventId(eventId)
            .eventType(MessageCreatedOutbox.EVENT_TYPE)
            .schemaVersion(MessageCreatedOutbox.SCHEMA_VERSION)
            .messageId(messageId)
            .roomId(command.roomId())
            .roomSequence(roomSequence)
            .senderId(command.senderId())
            .roomMemberId(roomMemberId)
            .clientMessageId(command.clientMessageId())
            .sender(command.sender())
            .messageBlocks(command.messageBlocks())
            .messageCreatedAt(createdAt)
            .occurredAt(createdAt)
            .publishedAt(null)
            .build());
        return new CreateChatMessageResult(true, message);
    }

    private Optional<ChatMessage> findDurableResult(CreateChatMessageCommand command) {
        return commandRepository.findMessage(
                command.roomId(),
                command.senderId(),
                command.clientMessageId()
            )
            .filter(message -> commandRepository.outboxExists(message.getId()));
    }
    private CreateChatMessageResult resolveCommittedResultOrThrow(
        CreateChatMessageCommand command,
        String attemptedMessageId,
        RuntimeException cause
    ) {
        for (int attempt = 0; attempt < DURABLE_RESULT_LOOKUP_ATTEMPTS; attempt++) {
            Optional<ChatMessage> durableResult = findDurableResult(command);
            if (durableResult.isPresent()) {
                ChatMessage message = durableResult.get();
                return new CreateChatMessageResult(
                    attemptedMessageId != null && attemptedMessageId.equals(message.getId()),
                    message
                );
            }
            if (attempt + 1 < DURABLE_RESULT_LOOKUP_ATTEMPTS) {
                try {
                    Thread.sleep(DURABLE_RESULT_LOOKUP_DELAY_MILLIS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        ChatMessageCommitUncertainException exception = new ChatMessageCommitUncertainException();
        exception.initCause(cause);
        throw exception;
    }

    private boolean isDuplicateKey(Throwable throwable) {
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            if (cause instanceof DuplicateKeyException) {
                return true;
            }
            if (cause instanceof MongoException mongoException
                && mongoException.getCode() == 11000) {
                return true;
            }
        }
        return false;
    }

    private boolean hasErrorLabel(Throwable throwable, String errorLabel) {
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            if (cause instanceof MongoException mongoException
                && mongoException.hasErrorLabel(errorLabel)) {
                return true;
            }
        }
        return false;
    }

    private static TransactionOperations transactionTemplate(
        MongoTransactionManager transactionManager
    ) {
        return new TransactionTemplate(transactionManager);
    }
}
