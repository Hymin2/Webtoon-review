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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@Tag("chat-message-gap-recovery")
class ChatMessageGapRecoveryScenarioTest {

    private static final Duration RECEIVE_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration CACHE_WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final ChatTestProperties TEST_USER = new ChatTestProperties(
        URI.create("http://localhost:18080"),
        URI.create("ws://localhost:18080/stomp-chat"),
        "chat_gap_recovery",
        "GapRecovery2026",
        "gap-recovery-client",
        "CHAT_TEST",
        Duration.ofSeconds(30)
    );

    @Test
    @DisplayName("Pub/Sub에서 누락된 메시지를 순번 차이로 감지하고 캐시에서 복구한다")
    void PubSub에서_누락된_메시지를_순번_차이로_감지하고_캐시에서_복구한다() {
        ChatTestContext context = new ChatTestFixture(
            TEST_USER,
            WebtoonFixtureProperties.fromEnvironment()
        ).prepareChatRoom();
        String testRunId = UUID.randomUUID().toString().substring(0, 8);
        String firstClientMessageId = "gap-" + testRunId + "-1";
        String skippedClientMessageId = "gap-skip-publish-" + testRunId;
        String thirdClientMessageId = "gap-" + testRunId + "-3";

        try (ChatTestClient client = new ChatTestClient(TEST_USER)) {
            client.connect(context.accessToken());
            client.subscribe(context.roomId());
            waitUntilSubscriptionIsReady(client, context, testRunId);

            client.send(createRequest(context.roomId(), firstClientMessageId, testRunId));
            ChatMessageResponse firstMessage = awaitMessage(client, firstClientMessageId);

            client.send(createRequest(context.roomId(), skippedClientMessageId, testRunId));
            awaitCachedMessage(
                context,
                firstMessage.messageSequence(),
                skippedClientMessageId
            );
            client.send(createRequest(context.roomId(), thirdClientMessageId, testRunId));
            ChatMessageResponse thirdMessage = awaitMessage(client, thirdClientMessageId);

            List<ChatMessageResponse> stompMessages = client.receivedMessages();
            boolean skippedMessageNotReceived = stompMessages.stream()
                .noneMatch(message -> skippedClientMessageId.equals(message.clientMessageId()));
            boolean sequenceGapDetected = thirdMessage.messageSequence()
                > firstMessage.messageSequence() + 1;

            List<ChatMessageResponse> cachedMessages = getMessagesAfter(
                context,
                firstMessage.messageSequence()
            );
            Set<String> testMessageIds = Set.of(
                firstClientMessageId,
                skippedClientMessageId,
                thirdClientMessageId
            );
            List<ChatMessageResponse> cachedTestMessages = cachedMessages.stream()
                .filter(message -> testMessageIds.contains(message.clientMessageId()))
                .toList();
            List<ChatMessageResponse> mergedMessages = mergeMessages(
                List.of(firstMessage, thirdMessage),
                cachedTestMessages
            );

            boolean missingMessageRecovered = mergedMessages.stream()
                .anyMatch(message -> skippedClientMessageId.equals(message.clientMessageId()));
            boolean duplicateRemoved = mergedMessages.stream()
                .filter(message -> thirdClientMessageId.equals(message.clientMessageId()))
                .count() == 1;
            List<Long> recoveredSequences = mergedMessages.stream()
                .map(ChatMessageResponse::messageSequence)
                .toList();
            List<Long> cachedSequences = cachedTestMessages.stream()
                .map(ChatMessageResponse::messageSequence)
                .toList();

            printResult(
                firstClientMessageId,
                skippedClientMessageId,
                thirdClientMessageId,
                sequenceGapDetected,
                firstMessage.messageSequence(),
                thirdMessage.messageSequence(),
                cachedSequences,
                missingMessageRecovered,
                duplicateRemoved,
                recoveredSequences
            );

            assertThat(client.connectionFailure()).isNull();
            assertThat(skippedMessageNotReceived).isTrue();
            assertThat(sequenceGapDetected).isTrue();
            assertThat(cachedTestMessages)
                .extracting(ChatMessageResponse::clientMessageId)
                .contains(skippedClientMessageId, thirdClientMessageId);
            assertThat(missingMessageRecovered).isTrue();
            assertThat(duplicateRemoved).isTrue();
            assertThat(mergedMessages)
                .extracting(ChatMessageResponse::clientMessageId)
                .containsExactly(
                    firstClientMessageId,
                    skippedClientMessageId,
                    thirdClientMessageId
                );
        }
    }

    private void waitUntilSubscriptionIsReady(
        ChatTestClient client,
        ChatTestContext context,
        String testRunId
    ) {
        String readyMessageId = "gap-ready-" + testRunId;
        client.send(createRequest(context.roomId(), readyMessageId, testRunId));
        assertThat(awaitMessage(client, readyMessageId)).isNotNull();
    }

    private ChatMessageResponse awaitMessage(ChatTestClient client, String clientMessageId) {
        ChatMessageResponse message = client.awaitMessage(
            response -> clientMessageId.equals(response.clientMessageId()),
            RECEIVE_TIMEOUT
        );
        assertThat(message).isNotNull();
        return message;
    }

    private List<ChatMessageResponse> getMessagesAfter(
        ChatTestContext context,
        long messageSequence
    ) {
        MessagesResponse response = RestClient.builder()
            .baseUrl(TEST_USER.httpUrl().toString())
            .build()
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/chat/room/{roomId}/messages")
                .queryParam("messageSequence", messageSequence)
                .build(context.roomId()))
            .header("Authorization", authorizationHeader(context.accessToken()))
            .retrieve()
            .body(MessagesResponse.class);

        if (response == null || response.data() == null) {
            return List.of();
        }
        return List.of(response.data());
    }

    private void awaitCachedMessage(
        ChatTestContext context,
        long afterSequence,
        String clientMessageId
    ) {
        long deadline = System.nanoTime() + CACHE_WAIT_TIMEOUT.toNanos();

        while (System.nanoTime() < deadline) {
            boolean cached = getMessagesAfter(context, afterSequence).stream()
                .anyMatch(message -> clientMessageId.equals(message.clientMessageId()));
            if (cached) {
                return;
            }
            sleepForCacheCheck();
        }
        throw new IllegalStateException(
            "Pub/Sub 생략 메시지가 Redis Cache에 저장되지 않았습니다: " + clientMessageId
        );
    }

    private void sleepForCacheCheck() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Redis Cache 확인 대기가 중단되었습니다.", exception);
        }
    }

    private List<ChatMessageResponse> mergeMessages(
        List<ChatMessageResponse> receivedMessages,
        List<ChatMessageResponse> cachedMessages
    ) {
        Map<String, ChatMessageResponse> messagesById = new LinkedHashMap<>();
        receivedMessages.forEach(message -> messagesById.put(message.messageId(), message));
        cachedMessages.forEach(message -> messagesById.putIfAbsent(message.messageId(), message));

        List<ChatMessageResponse> mergedMessages = new ArrayList<>(messagesById.values());
        mergedMessages.sort(Comparator.comparing(ChatMessageResponse::messageSequence));
        return List.copyOf(mergedMessages);
    }

    private ChatMessageRequest createRequest(
        long roomId,
        String clientMessageId,
        String testRunId
    ) {
        MessageBlock messageBlock = new MessageBlock(
            MessageBlockType.TEXT,
            "메시지 누락 복구 테스트 - " + clientMessageId,
            Map.of("source", "gap-recovery-test", "testRunId", testRunId)
        );
        return new ChatMessageRequest(roomId, clientMessageId, List.of(messageBlock));
    }

    private void printResult(
        String firstClientMessageId,
        String skippedClientMessageId,
        String thirdClientMessageId,
        boolean sequenceGapDetected,
        long firstSequence,
        long thirdSequence,
        List<Long> cachedSequences,
        boolean missingMessageRecovered,
        boolean duplicateRemoved,
        List<Long> recoveredSequences
    ) {
        System.out.println("[메시지 누락 복구 테스트 결과]");
        System.out.println();
        System.out.println("메시지 1 전송 (clientMessageId = " + firstClientMessageId + ")");
        System.out.println("메시지 2 전송 (clientMessageId = " + skippedClientMessageId + ")");
        System.out.println("메시지 3 전송 (clientMessageId = " + thirdClientMessageId + ")");
        System.out.println();
        System.out.println(
            "메시지 1 수신 성공 (clientMessageId = " + firstClientMessageId
                + ", sequence = " + firstSequence + ")"
        );
        System.out.println(
            "메시지 3 수신 성공 (clientMessageId = " + thirdClientMessageId
                + ", sequence = " + thirdSequence + ")"
        );
        System.out.println();
        System.out.println("순번 차이 감지: " + result(sequenceGapDetected));
        System.out.println();
        System.out.println("누락 메시지 조회 요청 기준 sequence: " + firstSequence);
        System.out.println("Redis Cache 조회 sequence: " + cachedSequences);
        System.out.println();
        System.out.println("누락 메시지 복구: " + result(missingMessageRecovered));
        System.out.println("중복 메시지 제거: " + result(duplicateRemoved));
        System.out.println(
            "복구 후 메시지 순서: "
                + recoveredSequences.stream()
                .map(String::valueOf)
                .reduce((left, right) -> left + " → " + right)
                .orElse("없음")
        );
        System.out.println();
        System.out.println(
            "최종 결과: "
                + (sequenceGapDetected && missingMessageRecovered && duplicateRemoved
                    ? "누락 메시지 복구 성공"
                    : "누락 메시지 복구 실패")
        );
    }

    private String result(boolean success) {
        return success ? "성공" : "실패";
    }

    private String authorizationHeader(String accessToken) {
        return accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
    }

    private record MessagesResponse(ChatMessageResponse[] data) {
    }
}
