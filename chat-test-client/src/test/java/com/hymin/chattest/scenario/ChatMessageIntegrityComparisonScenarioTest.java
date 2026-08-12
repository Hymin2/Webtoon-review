package com.hymin.chattest.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import com.hymin.chattest.client.ChatTestClient;
import com.hymin.chattest.contract.ChatMessageRequest;
import com.hymin.chattest.contract.ChatMessageResponse;
import com.hymin.chattest.contract.MessageBlock;
import com.hymin.chattest.contract.MessageBlockType;
import com.hymin.chattest.fixture.ChatTestContext;
import com.hymin.chattest.fixture.ChatTestFixture;
import com.hymin.chattest.fixture.WebtoonFixtureProperties;
import com.hymin.chattest.support.ChatTestProperties;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("chat-integrity")
class ChatMessageIntegrityComparisonScenarioTest {

    private static final int LOGICAL_MESSAGE_COUNT = 100;
    private static final int SEND_ATTEMPTS_PER_MESSAGE = 3;
    private static final Duration DUPLICATE_OBSERVATION_TIME = Duration.ofSeconds(3);
    private static final ChatTestProperties TEST_USER = new ChatTestProperties(
        URI.create("http://localhost:18080"),
        URI.create("ws://localhost:18080/stomp-chat"),
        "chat_integrity",
        "ChatIntegrity2026",
        "integrity-client",
        "CHAT_TEST",
        Duration.ofSeconds(30)
    );

    @Test
    @DisplayName("멱등성과 순번 적용 상태에 따른 메시지 무결성을 비교한다")
    void 멱등성과_순번_적용_상태에_따른_메시지_무결성을_비교한다() {
        IntegrityMode mode = IntegrityMode.fromEnvironment();
        ChatTestContext context = new ChatTestFixture(
            TEST_USER,
            WebtoonFixtureProperties.fromEnvironment()
        ).prepareChatRoom();
        String testRunId = "it-" + UUID.randomUUID().toString().substring(0, 8);
        List<String> clientMessageIds = createClientMessageIds(testRunId);
        int totalSendCount = LOGICAL_MESSAGE_COUNT * SEND_ATTEMPTS_PER_MESSAGE;

        try (ChatTestClient client = new ChatTestClient(TEST_USER)) {
            client.connect(context.accessToken());
            client.subscribe(context.roomId());
            clientMessageIds.forEach(clientMessageId ->
                IntStream.range(0, SEND_ATTEMPTS_PER_MESSAGE).forEach(attempt ->
                    client.send(createRequest(context.roomId(), clientMessageId, testRunId))
                )
            );

            assertThat(client.awaitMessageCount(mode.expectedReceiveCount(), TEST_USER.timeout()))
                .withFailMessage(
                    "메시지 수신 시간 초과: mode=%s, expected=%d, actual=%d",
                    mode,
                    mode.expectedReceiveCount(),
                    client.receivedMessages().size()
                )
                .isTrue();
            assertThat(client.awaitMessageCount(
                mode.expectedReceiveCount() + 1,
                DUPLICATE_OBSERVATION_TIME
            )).as("예상 개수를 초과한 메시지가 수신되지 않아야 한다").isFalse();
            assertThat(client.connectionFailure()).isNull();

            List<ChatMessageResponse> messages = client.receivedMessages().stream()
                .filter(message -> message.clientMessageId().startsWith(testRunId))
                .toList();
            IntegrityResult result = analyze(messages);

            printResult(mode, testRunId, totalSendCount, result);
            verifyResult(mode, context, messages, result, clientMessageIds);
        }
    }

    private List<String> createClientMessageIds(String testRunId) {
        return IntStream.rangeClosed(1, LOGICAL_MESSAGE_COUNT)
            .mapToObj(index -> "%s-%03d".formatted(testRunId, index))
            .toList();
    }

    private ChatMessageRequest createRequest(
        long roomId,
        String clientMessageId,
        String testRunId
    ) {
        MessageBlock messageBlock = new MessageBlock(
            MessageBlockType.TEXT,
            "메시지 무결성 비교 테스트 - " + clientMessageId,
            Map.of("source", "integrity-comparison-test", "testRunId", testRunId)
        );
        return new ChatMessageRequest(roomId, clientMessageId, List.of(messageBlock));
    }

    private IntegrityResult analyze(List<ChatMessageResponse> messages) {
        long uniqueClientMessageCount = messages.stream()
            .map(ChatMessageResponse::clientMessageId)
            .distinct()
            .count();
        List<Long> sequences = messages.stream()
            .map(ChatMessageResponse::messageSequence)
            .filter(sequence -> sequence != null)
            .toList();
        long uniqueSequenceCount = sequences.stream().distinct().count();

        int sequenceInversionCount = 0;
        for (int index = 1; index < sequences.size(); index++) {
            long previous = sequences.get(index - 1);
            long current = sequences.get(index);
            if (current < previous) {
                sequenceInversionCount++;
            }
        }
        List<Long> sortedSequences = sequences.stream().distinct().sorted().toList();
        long missingSequenceCount = 0;
        for (int index = 1; index < sortedSequences.size(); index++) {
            long previous = sortedSequences.get(index - 1);
            long current = sortedSequences.get(index);
            missingSequenceCount += Math.max(0, current - previous - 1);
        }

        return new IntegrityResult(
            messages.size(),
            uniqueClientMessageCount,
            messages.size() - uniqueClientMessageCount,
            sequences.size(),
            sequences.size() - uniqueSequenceCount,
            sequenceInversionCount,
            missingSequenceCount
        );
    }

    private void verifyResult(
        IntegrityMode mode,
        ChatTestContext context,
        List<ChatMessageResponse> messages,
        IntegrityResult result,
        List<String> expectedClientMessageIds
    ) {
        assertThat(result.receivedMessageCount()).isEqualTo(mode.expectedReceiveCount());
        assertThat(result.uniqueClientMessageCount()).isEqualTo(LOGICAL_MESSAGE_COUNT);
        assertThat(result.duplicateMessageCount()).isEqualTo(mode.expectedDuplicateCount());
        assertThat(messages).allSatisfy(message -> {
            assertThat(message.roomId()).isEqualTo(context.roomId());
            assertThat(message.roomMemberId()).isEqualTo(context.roomMemberId());
        });
        assertThat(messages.stream()
            .map(ChatMessageResponse::clientMessageId)
            .distinct()
            .toList()
        ).containsExactlyInAnyOrderElementsOf(expectedClientMessageIds);

        if (mode.sequenceEnabled()) {
            assertThat(result.sequenceProvidedCount()).isEqualTo(mode.expectedReceiveCount());
            assertThat(result.duplicateSequenceCount()).isZero();
        } else {
            assertThat(result.sequenceProvidedCount()).isZero();
        }
    }

    private void printResult(
        IntegrityMode mode,
        String testRunId,
        int totalSendCount,
        IntegrityResult result
    ) {
        System.out.println("[메시지 멱등성·순번 비교 결과]");
        System.out.println("적용 모드: " + mode);
        System.out.println("테스트 실행 ID: " + testRunId);
        System.out.println("논리 메시지 수: " + LOGICAL_MESSAGE_COUNT + "개");
        System.out.println("메시지당 전송 횟수: " + SEND_ATTEMPTS_PER_MESSAGE + "회");
        System.out.println("전체 전송 요청 수: " + totalSendCount + "개");
        System.out.println("수신 메시지 수: " + result.receivedMessageCount() + "개");
        System.out.println("고유 clientMessageId 수: " + result.uniqueClientMessageCount() + "개");
        System.out.println("논리 중복 메시지 수: " + result.duplicateMessageCount() + "개");
        System.out.println("순번 제공 메시지 수: " + result.sequenceProvidedCount() + "개");
        System.out.println("중복 순번 수: " + result.duplicateSequenceCount() + "개");
        System.out.println("순서 역전 횟수: " + result.sequenceInversionCount() + "회");
        System.out.println("누락 순번 수: " + result.missingSequenceCount() + "개");
    }

    private enum IntegrityMode {
        NONE(false, false),
        IDEMPOTENCY_ONLY(true, false),
        SEQUENCE_ONLY(false, true);

        private final boolean idempotencyEnabled;
        private final boolean sequenceEnabled;

        IntegrityMode(boolean idempotencyEnabled, boolean sequenceEnabled) {
            this.idempotencyEnabled = idempotencyEnabled;
            this.sequenceEnabled = sequenceEnabled;
        }

        static IntegrityMode fromEnvironment() {
            String value = System.getenv().getOrDefault(
                "CHAT_TEST_INTEGRITY_MODE",
                "SEQUENCE_ONLY"
            );
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        }

        int expectedReceiveCount() {
            return idempotencyEnabled
                ? LOGICAL_MESSAGE_COUNT
                : LOGICAL_MESSAGE_COUNT * SEND_ATTEMPTS_PER_MESSAGE;
        }

        int expectedDuplicateCount() {
            return expectedReceiveCount() - LOGICAL_MESSAGE_COUNT;
        }

        boolean sequenceEnabled() {
            return sequenceEnabled;
        }
    }

    private record IntegrityResult(
        int receivedMessageCount,
        long uniqueClientMessageCount,
        long duplicateMessageCount,
        int sequenceProvidedCount,
        long duplicateSequenceCount,
        int sequenceInversionCount,
        long missingSequenceCount
    ) {
    }
}
