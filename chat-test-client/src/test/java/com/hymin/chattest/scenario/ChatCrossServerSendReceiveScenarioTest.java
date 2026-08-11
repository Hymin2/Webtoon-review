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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("chat-cross-server")
class ChatCrossServerSendReceiveScenarioTest {

    private static final ChatTestProperties USER_A = new ChatTestProperties(
        "chat_cross_user_a",
        "ChatCrossA2026",
        "cross-server-client-a",
        URI.create("ws://localhost:18081/stomp-chat")
    );
    private static final ChatTestProperties USER_B = new ChatTestProperties(
        "chat_cross_user_b",
        "ChatCrossB2026",
        "cross-server-client-b",
        URI.create("ws://localhost:18082/stomp-chat")
    );

    @Test
    @DisplayName("서로 다른 채팅 서버에 연결된 두 사용자가 메시지를 송수신한다")
    void 두_채팅_서버에_연결된_사용자가_메시지를_송수신한다() {
        ChatTestContext contextA = prepareChatRoom(USER_A);
        ChatTestContext contextB = prepareChatRoom(USER_B);
        Duration timeout = USER_A.timeout();

        assertThat(contextA.roomId()).isEqualTo(contextB.roomId());
        assertThat(contextA.roomMemberId()).isNotEqualTo(contextB.roomMemberId());

        try (ChatTestClient clientA = new ChatTestClient(USER_A);
             ChatTestClient clientB = new ChatTestClient(USER_B)) {
            clientA.connect(contextA.accessToken());
            clientB.connect(contextB.accessToken());

            clientA.subscribe(contextA.roomId());
            awaitOwnSubscription(clientA, contextA, timeout);
            clientB.subscribe(contextB.roomId());

            verifyDelivery(clientB, clientA, contextB, "B-to-A", timeout);
            verifyDelivery(clientA, clientB, contextA, "A-to-B", timeout);

            assertThat(clientA.connectionFailure()).isNull();
            assertThat(clientB.connectionFailure()).isNull();
        }

        System.out.println("[교차 서버 메시지 송수신 결과]");
        System.out.println("A → B: 성공");
        System.out.println("B → A: 성공");
    }

    private ChatTestContext prepareChatRoom(ChatTestProperties properties) {
        return new ChatTestFixture(
            properties,
            WebtoonFixtureProperties.fromEnvironment()
        ).prepareChatRoom();
    }

    private void awaitOwnSubscription(
        ChatTestClient client,
        ChatTestContext context,
        Duration timeout
    ) {
        String clientMessageId = UUID.randomUUID().toString();
        client.send(createRequest(context.roomId(), clientMessageId, "subscription-ready"));

        assertThat(awaitMessage(client, clientMessageId, context, timeout)).isNotNull();
    }

    private void verifyDelivery(
        ChatTestClient sender,
        ChatTestClient receiver,
        ChatTestContext senderContext,
        String content,
        Duration timeout
    ) {
        String clientMessageId = UUID.randomUUID().toString();
        sender.send(createRequest(senderContext.roomId(), clientMessageId, content));

        ChatMessageResponse senderMessage = awaitMessage(
            sender, clientMessageId, senderContext, timeout);
        ChatMessageResponse receiverMessage = awaitMessage(
            receiver, clientMessageId, senderContext, timeout);

        assertThat(senderMessage).isNotNull();
        assertThat(receiverMessage).isNotNull();
        assertThat(receiverMessage.messageSequence())
            .isEqualTo(senderMessage.messageSequence());
    }

    private ChatMessageResponse awaitMessage(
        ChatTestClient client,
        String clientMessageId,
        ChatTestContext senderContext,
        Duration timeout
    ) {
        return client.awaitMessage(
            message -> clientMessageId.equals(message.clientMessageId())
                && senderContext.roomMemberId().equals(message.roomMemberId()),
            timeout
        );
    }

    private ChatMessageRequest createRequest(
        long roomId,
        String clientMessageId,
        String content
    ) {
        MessageBlock messageBlock = new MessageBlock(
            MessageBlockType.TEXT,
            content,
            Map.of("source", "cross-server-test")
        );
        return new ChatMessageRequest(roomId, clientMessageId, List.of(messageBlock));
    }
}
