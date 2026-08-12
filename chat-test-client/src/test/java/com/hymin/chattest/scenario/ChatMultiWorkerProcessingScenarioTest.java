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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("chat-multi-worker")
class ChatMultiWorkerProcessingScenarioTest {

    private static final int ROOM_COUNT = 100;
    private static final ChatTestProperties TEST_USER = new ChatTestProperties(
        URI.create("http://localhost:18080"),
        URI.create("ws://localhost:18080/stomp-chat"),
        "chat_multi_worker",
        "ChatMultiWorker2026",
        "multi-worker-client",
        "CHAT_TEST",
        Duration.ofSeconds(30)
    );

    @Test
    @DisplayName("여러 채팅방의 메시지를 다중 워커 환경에서 처리한다")
    void 여러_채팅방의_메시지를_다중_워커_환경에서_처리한다() {
        List<ChatTestContext> rooms = new ChatTestFixture(
            TEST_USER,
            WebtoonFixtureProperties.fromEnvironment()
        ).prepareChatRooms(ROOM_COUNT);
        String testRunId = "mw-" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, ChatTestContext> expectedMessages = createExpectedMessages(testRunId, rooms);

        try (ChatTestClient client = new ChatTestClient(TEST_USER)) {
            client.connect(rooms.get(0).accessToken());
            rooms.forEach(room -> client.subscribe(room.roomId()));
            expectedMessages.forEach((clientMessageId, room) ->
                client.send(createRequest(room.roomId(), clientMessageId, testRunId))
            );

            boolean receivedAll = client.awaitMessageCount(ROOM_COUNT, TEST_USER.timeout());
            assertThat(receivedAll)
                .withFailMessage(
                    "메시지 수신 시간 초과: expected=%d, actual=%d, connectionFailure=%s",
                    ROOM_COUNT,
                    client.receivedMessages().size(),
                    client.connectionFailure()
                )
                .isTrue();
            assertThat(client.connectionFailure()).isNull();

            List<ChatMessageResponse> testMessages = client.receivedMessages().stream()
                .filter(message -> message.clientMessageId().startsWith(testRunId))
                .toList();

            assertThat(testMessages).hasSize(ROOM_COUNT);
            assertThat(testMessages)
                .extracting(ChatMessageResponse::clientMessageId)
                .doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(expectedMessages.keySet());
            testMessages.forEach(message -> verifyMessage(message, expectedMessages));

            printResult(testRunId, rooms.size(), testMessages.size());
        }
    }

    private Map<String, ChatTestContext> createExpectedMessages(
        String testRunId,
        List<ChatTestContext> rooms
    ) {
        return rooms.stream().collect(Collectors.toMap(
            room -> testRunId + "-room-" + room.roomId(),
            Function.identity(),
            (first, second) -> first,
            LinkedHashMap::new
        ));
    }

    private ChatMessageRequest createRequest(
        long roomId,
        String clientMessageId,
        String testRunId
    ) {
        MessageBlock messageBlock = new MessageBlock(
            MessageBlockType.TEXT,
            "다중 워커 테스트 메시지 - roomId=" + roomId,
            Map.of("source", "multi-worker-test", "testRunId", testRunId)
        );
        return new ChatMessageRequest(roomId, clientMessageId, List.of(messageBlock));
    }

    private void verifyMessage(
        ChatMessageResponse message,
        Map<String, ChatTestContext> expectedMessages
    ) {
        ChatTestContext expectedRoom = expectedMessages.get(message.clientMessageId());

        assertThat(expectedRoom).isNotNull();
        assertThat(message.roomId()).isEqualTo(expectedRoom.roomId());
        assertThat(message.roomMemberId()).isEqualTo(expectedRoom.roomMemberId());
        assertThat(message.messageSequence()).isPositive();
    }

    private void printResult(String testRunId, int roomCount, int receivedMessageCount) {
        System.out.println("[다중 채팅 워커 메시지 처리 결과]");
        System.out.println("테스트 실행 ID: " + testRunId);
        System.out.println("실행 워커 수: 3개");
        System.out.println("채팅방 수: " + roomCount + "개");
        System.out.println("전송 메시지 수: " + roomCount + "개");
        System.out.println("수신 메시지 수: " + receivedMessageCount + "개");
        System.out.println("메시지 유실: " + (roomCount - receivedMessageCount) + "개");
        System.out.println("메시지 중복: 0개");
        System.out.println("워커별 처리 결과: Grafana에서 테스트 실행 ID로 확인");
    }
}
