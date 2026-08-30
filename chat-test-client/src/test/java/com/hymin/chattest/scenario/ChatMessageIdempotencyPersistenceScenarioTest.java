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
import com.hymin.chattest.support.MongoChatMessageProbe;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("chat-message-idempotency")
class ChatMessageIdempotencyPersistenceScenarioTest {

    private static final int SEND_COUNT = 10;
    private static final Duration RECEIVE_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration PERSIST_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration PERSIST_SETTLE_TIME = Duration.ofSeconds(2);
    private static final ChatTestProperties TEST_USER = new ChatTestProperties(
        URI.create("http://localhost:18080"),
        URI.create("ws://localhost:18080/stomp-chat"),
        "chat_idempotency",
        "Idempotency2026",
        "idempotency-client",
        "CHAT_TEST",
        Duration.ofSeconds(30)
    );

    @Test
    @DisplayName("동일한 메시지를 여러 번 전송해도 같은 메시지 ID로 한 건만 저장한다")
    void 동일한_메시지를_여러_번_전송해도_같은_메시지_ID로_한_건만_저장한다() {
        ChatTestContext context = new ChatTestFixture(
            TEST_USER,
            WebtoonFixtureProperties.fromEnvironment()
        ).prepareChatRoom();
        String testRunId = "idem-" + UUID.randomUUID().toString().substring(0, 8);
        String clientMessageId = testRunId + "-message";
        ChatMessageRequest request = createRequest(context.roomId(), clientMessageId, testRunId);

        try (
            ChatTestClient client = new ChatTestClient(TEST_USER);
            MongoChatMessageProbe mongoProbe = MongoChatMessageProbe.fromEnvironment()
        ) {
            client.connect(context.accessToken());
            client.subscribe(context.roomId());
            waitUntilSubscriptionIsReady(client, context, testRunId);

            for (int count = 0; count < SEND_COUNT; count++) {
                client.send(request);
            }

            List<ChatMessageResponse> receivedMessages = awaitMessages(
                client,
                clientMessageId,
                SEND_COUNT,
                RECEIVE_TIMEOUT
            );
            List<String> serverMessageIds = receivedMessages.stream()
                .map(ChatMessageResponse::messageId)
                .distinct()
                .toList();
            long uniqueClientMessageIdCount = receivedMessages.stream()
                .map(ChatMessageResponse::clientMessageId)
                .distinct()
                .count();

            Document persistedMessage = mongoProbe.awaitMessage(
                context.roomId(),
                clientMessageId,
                PERSIST_TIMEOUT
            );
            waitForPersistenceToSettle();
            long persistedMessageCount = mongoProbe.countMessages(
                context.roomId(),
                clientMessageId
            );
            String persistedServerMessageId = persistedMessage.getString("_id");

            printResult(
                clientMessageId,
                receivedMessages.size(),
                uniqueClientMessageIdCount,
                serverMessageIds,
                persistedMessageCount,
                persistedServerMessageId
            );

            assertThat(client.connectionFailure()).isNull();
            assertThat(receivedMessages).hasSize(SEND_COUNT);
            assertThat(receivedMessages)
                .extracting(ChatMessageResponse::clientMessageId)
                .containsOnly(clientMessageId);
            assertThat(serverMessageIds).hasSize(1).doesNotContainNull();
            assertThat(persistedMessageCount).isOne();
            assertThat(persistedServerMessageId).isEqualTo(serverMessageIds.get(0));
        }
    }

    private void waitUntilSubscriptionIsReady(
        ChatTestClient client,
        ChatTestContext context,
        String testRunId
    ) {
        String readyMessageId = testRunId + "-subscription-ready";
        client.send(createRequest(context.roomId(), readyMessageId, testRunId));
        ChatMessageResponse readyMessage = client.awaitMessage(
            message -> readyMessageId.equals(message.clientMessageId()),
            RECEIVE_TIMEOUT
        );
        assertThat(readyMessage).isNotNull();
    }

    private List<ChatMessageResponse> awaitMessages(
        ChatTestClient client,
        String clientMessageId,
        int expectedCount,
        Duration timeout
    ) {
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            List<ChatMessageResponse> messages = messagesByClientMessageId(
                client,
                clientMessageId
            );
            if (messages.size() >= expectedCount) {
                return messages;
            }
            sleep(Duration.ofMillis(100), "메시지 수신 대기가 중단되었습니다.");
        }
        return messagesByClientMessageId(client, clientMessageId);
    }

    private List<ChatMessageResponse> messagesByClientMessageId(
        ChatTestClient client,
        String clientMessageId
    ) {
        return client.receivedMessages().stream()
            .filter(message -> clientMessageId.equals(message.clientMessageId()))
            .toList();
    }

    private ChatMessageRequest createRequest(
        long roomId,
        String clientMessageId,
        String testRunId
    ) {
        MessageBlock messageBlock = new MessageBlock(
            MessageBlockType.TEXT,
            "메시지 멱등성 저장 테스트 - " + testRunId,
            Map.of("source", "idempotency-persistence-test", "testRunId", testRunId)
        );
        return new ChatMessageRequest(roomId, clientMessageId, List.of(messageBlock));
    }

    private void waitForPersistenceToSettle() {
        sleep(PERSIST_SETTLE_TIME, "MongoDB 중복 저장 확인 대기가 중단되었습니다.");
    }

    private void printResult(
        String clientMessageId,
        int receivedMessageCount,
        long uniqueClientMessageIdCount,
        List<String> serverMessageIds,
        long persistedMessageCount,
        String persistedServerMessageId
    ) {
        System.out.println("[메시지 멱등성 테스트 결과]");
        System.out.println();
        System.out.println("동일 메시지 전송 횟수: " + SEND_COUNT + "회");
        System.out.println("수신 횟수: " + receivedMessageCount + "회");
        System.out.println();
        System.out.println("clientMessageId: " + clientMessageId);
        System.out.println(
            "serverMessageId: " + (serverMessageIds.isEmpty() ? "없음" : serverMessageIds.get(0))
        );
        System.out.println();
        System.out.println("고유 clientMessageId 수: " + uniqueClientMessageIdCount + "개");
        System.out.println("고유 serverMessageId 수: " + serverMessageIds.size() + "개");
        System.out.println();
        System.out.println("MongoDB 실제 저장 건수: " + persistedMessageCount + "건");
        System.out.println(
            "클라이언트/DB serverMessageId 일치: "
                + (!serverMessageIds.isEmpty()
                    && serverMessageIds.get(0).equals(persistedServerMessageId)
                    ? "성공"
                    : "실패")
        );
        System.out.println();
        System.out.println(
            "최종 결과: "
                + (receivedMessageCount == SEND_COUNT
                    && serverMessageIds.size() == 1
                    && persistedMessageCount == 1
                    ? "메시지 멱등성 보장"
                    : "메시지 멱등성 검증 실패")
        );
    }

    private void sleep(Duration duration, String errorMessage) {
        try {
            TimeUnit.NANOSECONDS.sleep(duration.toNanos());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(errorMessage, exception);
        }
    }
}
