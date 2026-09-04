package com.hymin.webtoon_review.chat.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.entity.MessageCreatedOutbox;
import com.hymin.webtoon_review.chat.common.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.common.repository.UserChatRoomRepository;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageCommand;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageResult;
import com.hymin.webtoon_review.chat.server.exception.ChatMessageCommitUncertainException;
import com.hymin.webtoon_review.chat.server.metrics.ChatMessageCommandMetrics;
import com.hymin.webtoon_review.chat.server.repository.ChatMessageCommandRepository;
import com.mongodb.MongoException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

class CreateChatMessageCommandServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-09-02T04:00:00.123Z"),
        ZoneOffset.UTC
    );

    private UserChatRoomRepository userChatRoomRepository;
    private ChatMessageCommandRepository commandRepository;
    private TransactionOperations transactionOperations;
    private ChatMessageCommandMetrics metrics;
    private CreateChatMessageCommandService service;

    @BeforeEach
    void setUp() {
        userChatRoomRepository = mock(UserChatRoomRepository.class);
        commandRepository = mock(ChatMessageCommandRepository.class);
        transactionOperations = mock(TransactionOperations.class);
        metrics = mock(ChatMessageCommandMetrics.class);
        service = new CreateChatMessageCommandService(
            userChatRoomRepository,
            commandRepository,
            metrics,
            transactionOperations,
            3,
            FIXED_CLOCK
        );
    }

    @Test
    void createsSequenceMessageAndOutboxInsideOneTransaction() {
        CreateChatMessageCommand command = command();
        when(userChatRoomRepository.findRoomMemberId(7L, 11L))
            .thenReturn(Optional.of("member-7"));
        when(commandRepository.findMessage(11L, 7L, "client-1"))
            .thenReturn(Optional.empty());
        when(commandRepository.allocateNextRoomSequence(11L)).thenReturn(1L);
        when(transactionOperations.execute(any())).thenAnswer(runTransactionBody());

        CreateChatMessageResult result = service.create(command);

        assertThat(result.created()).isTrue();
        assertThat(result.message().getMessageSequence()).isEqualTo(1L);
        assertThat(result.message().getCreatedAt()).isEqualTo("2026-09-02 04:00:00.123");
        InOrder order = inOrder(commandRepository);
        order.verify(commandRepository).allocateNextRoomSequence(11L);
        order.verify(commandRepository).insertMessage(any(ChatMessage.class));
        order.verify(commandRepository).insertOutbox(any(MessageCreatedOutbox.class));
    }

    @Test
    void returnsExistingDurableMessageWithoutStartingTransaction() {
        ChatMessage existing = existingMessage();
        when(userChatRoomRepository.findRoomMemberId(7L, 11L))
            .thenReturn(Optional.of("member-7"));
        when(commandRepository.findMessage(11L, 7L, "client-1"))
            .thenReturn(Optional.of(existing));
        when(commandRepository.outboxExists("message-1")).thenReturn(true);

        CreateChatMessageResult result = service.create(command());

        assertThat(result.created()).isFalse();
        assertThat(result.message()).isSameAs(existing);
        verify(transactionOperations, never()).execute(any());
        verify(commandRepository, never()).allocateNextRoomSequence(any());
    }

    @Test
    void membershipFailureDoesNotStartMongoTransaction() {
        when(userChatRoomRepository.findRoomMemberId(7L, 11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(command()))
            .isInstanceOf(InvalidChatRoomAccessException.class);

        verify(transactionOperations, never()).execute(any());
        verify(commandRepository, never()).allocateNextRoomSequence(any());
    }

    @Test
    void retriesWholeTransactionForTransientTransactionError() {
        when(userChatRoomRepository.findRoomMemberId(7L, 11L))
            .thenReturn(Optional.of("member-7"));
        when(commandRepository.findMessage(11L, 7L, "client-1"))
            .thenReturn(Optional.empty());
        when(commandRepository.allocateNextRoomSequence(11L)).thenReturn(1L);
        MongoException transientError = new MongoException(112, "write conflict");
        transientError.addLabel("TransientTransactionError");
        when(transactionOperations.execute(any()))
            .thenThrow(transientError)
            .thenAnswer(runTransactionBody());

        CreateChatMessageResult result = service.create(command());

        assertThat(result.created()).isTrue();
        verify(transactionOperations, times(2)).execute(any());
        verify(metrics).transactionRetry();
    }

    @Test
    void recordsRetryExhaustionAndDoesNotRetryForever() {
        when(userChatRoomRepository.findRoomMemberId(7L, 11L))
            .thenReturn(Optional.of("member-7"));
        when(commandRepository.findMessage(11L, 7L, "client-1"))
            .thenReturn(Optional.empty());
        MongoException transientError = new MongoException(112, "write conflict");
        transientError.addLabel("TransientTransactionError");
        when(transactionOperations.execute(any())).thenThrow(transientError);

        assertThatThrownBy(() -> service.create(command())).isSameAs(transientError);

        verify(transactionOperations, times(3)).execute(any());
        verify(metrics, times(2)).transactionRetry();
        verify(metrics).transactionRetryExhausted();
    }

    @Test
    void unknownCommitResultReturnsCreatedWhenThisAttemptCommitted() {
        AtomicInteger lookupCount = new AtomicInteger();
        AtomicReference<ChatMessage> committed = new AtomicReference<>();
        when(userChatRoomRepository.findRoomMemberId(7L, 11L))
            .thenReturn(Optional.of("member-7"));
        when(commandRepository.findMessage(11L, 7L, "client-1"))
            .thenAnswer(invocation -> lookupCount.incrementAndGet() < 3
                ? Optional.empty()
                : Optional.of(committed.get()));
        when(commandRepository.outboxExists(any())).thenReturn(true);
        when(commandRepository.allocateNextRoomSequence(11L)).thenReturn(1L);
        MongoException unknownCommit = new MongoException(91, "commit reply lost");
        unknownCommit.addLabel("UnknownTransactionCommitResult");
        when(transactionOperations.execute(any())).thenAnswer(invocation -> {
            ChatMessage created = ((CreateChatMessageResult) ((TransactionCallback<?>) invocation
                .getArgument(0)).doInTransaction(mock(TransactionStatus.class))).message();
            committed.set(created);
            throw unknownCommit;
        });

        CreateChatMessageResult result = service.create(command());

        assertThat(result.created()).isTrue();
        assertThat(result.message()).isSameAs(committed.get());
        verify(transactionOperations).execute(any());
    }

    @Test
    void unknownCommitResultReturnsExistingResultWhenAnotherAttemptCommitted() {
        ChatMessage committed = existingMessage();
        when(userChatRoomRepository.findRoomMemberId(7L, 11L))
            .thenReturn(Optional.of("member-7"));
        when(commandRepository.findMessage(11L, 7L, "client-1"))
            .thenReturn(Optional.empty(), Optional.of(committed));
        when(commandRepository.outboxExists("message-1")).thenReturn(true);
        MongoException unknownCommit = new MongoException(91, "commit reply lost");
        unknownCommit.addLabel("UnknownTransactionCommitResult");
        when(transactionOperations.execute(any())).thenThrow(unknownCommit);

        CreateChatMessageResult result = service.create(command());

        assertThat(result.created()).isFalse();
        assertThat(result.message()).isSameAs(committed);
        verify(transactionOperations).execute(any());
    }

    @Test
    void unknownCommitResultIsNotReportedAsSuccessWhenDurableResultIsMissing() {
        when(userChatRoomRepository.findRoomMemberId(7L, 11L))
            .thenReturn(Optional.of("member-7"));
        when(commandRepository.findMessage(11L, 7L, "client-1"))
            .thenReturn(Optional.empty());
        MongoException unknownCommit = new MongoException(91, "commit reply lost");
        unknownCommit.addLabel("UnknownTransactionCommitResult");
        when(transactionOperations.execute(any())).thenThrow(unknownCommit);

        assertThatThrownBy(() -> service.create(command()))
            .isInstanceOf(ChatMessageCommitUncertainException.class)
            .hasCause(unknownCommit);

        verify(transactionOperations).execute(any());
    }

    private Answer<Object> runTransactionBody() {
        return invocation -> ((TransactionCallback<?>) invocation.getArgument(0))
            .doInTransaction(mock(TransactionStatus.class));
    }

    private CreateChatMessageCommand command() {
        return new CreateChatMessageCommand(11L, 7L, "sender", "client-1", List.of());
    }

    private ChatMessage existingMessage() {
        return ChatMessage.builder()
            .id("message-1")
            .roomId(11L)
            .senderId(7L)
            .roomMemberId("member-7")
            .messageSequence(1L)
            .clientMessageId("client-1")
            .sender("sender")
            .createdAt("2026-09-02 04:00:00.123")
            .messageBlocks(List.of())
            .build();
    }
}
