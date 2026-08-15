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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("chat-worker-failure")
class ChatWorkerFailureSendReceiveScenarioTest {

    private static final int ROOM_COUNT = 5;
    private static final int REGULAR_MESSAGE_COUNT = 100;
    private static final Duration SEND_DURATION = Duration.ofSeconds(10);
    private static final Duration FAILURE_AT = Duration.ofSeconds(5);
    private static final Duration SEND_INTERVAL = Duration.ofMillis(100);
    private static final Duration RECEIVE_TIMEOUT = Duration.ofSeconds(45);
    private static final ChatTestProperties TEST_USER = new ChatTestProperties(
        URI.create("http://localhost:18080"),
        URI.create("ws://localhost:18080/stomp-chat"),
        "chat_worker_failure",
        "WorkerFail2026",
        "worker-failure-client",
        "CHAT_TEST",
        Duration.ofSeconds(30)
    );

    @Test
    @DisplayName("여러 워커 중 1개가 종료되어도 전송한 메시지를 모두 수신한다")
    void 워커_하나가_종료되어도_전송한_메시지를_모두_수신한다() {
        List<ChatTestContext> rooms = new ChatTestFixture(
            TEST_USER,
            WebtoonFixtureProperties.fromEnvironment()
        ).prepareChatRooms(ROOM_COUNT);
        String testRunId = "wf-" + UUID.randomUUID().toString().substring(0, 8);
        Set<String> sentMessageIds = new LinkedHashSet<>();
        Set<String> stopMessageIds = new LinkedHashSet<>();
        Map<String, Long> stopMessageArrivalMillis = new LinkedHashMap<>();

        try (ChatTestClient client = new ChatTestClient(TEST_USER)) {
            client.connect(rooms.get(0).accessToken());
            rooms.forEach(room -> client.subscribe(room.roomId()));

            long startedAt = System.nanoTime();
            sendMessages(
                client,
                rooms,
                testRunId,
                sentMessageIds,
                stopMessageIds,
                stopMessageArrivalMillis,
                startedAt
            );
            awaitAllMessages(
                client,
                sentMessageIds,
                stopMessageIds,
                stopMessageArrivalMillis,
                startedAt
            );

            List<ChatMessageResponse> receivedMessages = client.receivedMessages().stream()
                .filter(message -> sentMessageIds.contains(message.clientMessageId()))
                .toList();
            long uniqueReceivedCount = receivedMessages.stream()
                .map(ChatMessageResponse::clientMessageId)
                .distinct()
                .count();
            long uniqueStopMessageCount = receivedMessages.stream()
                .map(ChatMessageResponse::clientMessageId)
                .filter(stopMessageIds::contains)
                .distinct()
                .count();

            printResult(
                testRunId,
                sentMessageIds.size(),
                receivedMessages.size(),
                uniqueReceivedCount,
                uniqueStopMessageCount,
                stopMessageArrivalMillis
            );

            assertThat(client.connectionFailure()).isNull();
            assertThat(sentMessageIds).hasSize(REGULAR_MESSAGE_COUNT + ROOM_COUNT);
            assertThat(uniqueReceivedCount).isEqualTo(sentMessageIds.size());
            assertThat(uniqueStopMessageCount).isEqualTo(ROOM_COUNT);
        }
    }

    private void sendMessages(
        ChatTestClient client,
        List<ChatTestContext> rooms,
        String testRunId,
        Set<String> sentMessageIds,
        Set<String> stopMessageIds,
        Map<String, Long> stopMessageArrivalMillis,
        long startedAt
    ) {
        boolean stopMessagesSent = false;

        for (int index = 0; index < REGULAR_MESSAGE_COUNT; index++) {
            waitUntil(startedAt + SEND_INTERVAL.toNanos() * index);

            if (!stopMessagesSent && System.nanoTime() - startedAt >= FAILURE_AT.toNanos()) {
                sendStopMessages(client, rooms, testRunId, sentMessageIds, stopMessageIds);
                stopMessagesSent = true;
            }

            ChatTestContext room = rooms.get(index % rooms.size());
            String clientMessageId = "%s-%04d".formatted(testRunId, index + 1);
            client.send(createRequest(room.roomId(), clientMessageId, testRunId));
            sentMessageIds.add(clientMessageId);
            captureStopMessageArrivals(
                client,
                stopMessageIds,
                stopMessageArrivalMillis,
                startedAt
            );
        }

        waitUntil(startedAt + SEND_DURATION.toNanos());
        assertThat(stopMessagesSent).isTrue();
    }

    private void sendStopMessages(
        ChatTestClient client,
        List<ChatTestContext> rooms,
        String testRunId,
        Set<String> sentMessageIds,
        Set<String> stopMessageIds
    ) {
        for (ChatTestContext room : rooms) {
            String clientMessageId = "wf-stop-%s-room-%d".formatted(testRunId, room.roomId());
            client.send(createRequest(room.roomId(), clientMessageId, testRunId));
            sentMessageIds.add(clientMessageId);
            stopMessageIds.add(clientMessageId);
        }
    }

    private void awaitAllMessages(
        ChatTestClient client,
        Set<String> sentMessageIds,
        Set<String> stopMessageIds,
        Map<String, Long> stopMessageArrivalMillis,
        long startedAt
    ) {
        long deadline = System.nanoTime() + RECEIVE_TIMEOUT.toNanos();

        while (System.nanoTime() < deadline) {
            captureStopMessageArrivals(
                client,
                stopMessageIds,
                stopMessageArrivalMillis,
                startedAt
            );
            long uniqueReceivedCount = client.receivedMessages().stream()
                .map(ChatMessageResponse::clientMessageId)
                .filter(sentMessageIds::contains)
                .distinct()
                .count();

            if (uniqueReceivedCount == sentMessageIds.size()) {
                return;
            }
            waitUntil(System.nanoTime() + Duration.ofMillis(100).toNanos());
        }
    }

    private void captureStopMessageArrivals(
        ChatTestClient client,
        Set<String> stopMessageIds,
        Map<String, Long> stopMessageArrivalMillis,
        long startedAt
    ) {
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
        client.receivedMessages().stream()
            .map(ChatMessageResponse::clientMessageId)
            .filter(stopMessageIds::contains)
            .forEach(clientMessageId ->
                stopMessageArrivalMillis.putIfAbsent(clientMessageId, elapsedMillis)
            );
    }

    private ChatMessageRequest createRequest(
        long roomId,
        String clientMessageId,
        String testRunId
    ) {
        MessageBlock messageBlock = new MessageBlock(
            MessageBlockType.TEXT,
            "워커 장애 송수신 테스트 - " + clientMessageId,
            Map.of("source", "worker-failure-test", "testRunId", testRunId)
        );
        return new ChatMessageRequest(roomId, clientMessageId, List.of(messageBlock));
    }

    private void printResult(
        String testRunId,
        int sentMessageCount,
        int receivedMessageCount,
        long uniqueReceivedCount,
        long uniqueStopMessageCount,
        Map<String, Long> stopMessageArrivalMillis
    ) {
        System.out.println("[워커 장애 시 메시지 송수신 결과]");
        System.out.println("테스트 실행 ID: " + testRunId);
        System.out.println("채팅방 수: " + ROOM_COUNT + "개");
        System.out.println("시뮬레이션 시간: " + SEND_DURATION.toSeconds() + "초");
        System.out.println("종료 메시지 전송 시점: " + FAILURE_AT.toSeconds() + "초");
        System.out.println("종료 메시지 수신: " + uniqueStopMessageCount + "/" + ROOM_COUNT + "개");
        stopMessageArrivalMillis.forEach((clientMessageId, arrivalMillis) ->
            System.out.printf(
                "종료 메시지 도착: %s → %.2f초%n",
                clientMessageId,
                arrivalMillis / 1000.0
            )
        );
        System.out.println("전송 메시지 수: " + sentMessageCount + "개");
        System.out.println("수신 메시지 수: " + receivedMessageCount + "개");
        System.out.println("고유 수신 메시지 수: " + uniqueReceivedCount + "개");
        System.out.println(
            "최종 결과: "
                + (uniqueReceivedCount == sentMessageCount
                    ? "모든 전송 메시지 수신 성공"
                    : "미수신 메시지 발생")
        );
    }

    private void waitUntil(long targetNanoTime) {
        long remaining = targetNanoTime - System.nanoTime();
        if (remaining <= 0) {
            return;
        }
        try {
            TimeUnit.NANOSECONDS.sleep(remaining);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("워커 장애 테스트 대기가 중단되었습니다.", exception);
        }
    }
}
