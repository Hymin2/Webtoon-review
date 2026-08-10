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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@Tag("chat-basic")
@EnabledIfEnvironmentVariable(named = "CHAT_TEST_USERNAME", matches = ".+")
@EnabledIfEnvironmentVariable(named = "CHAT_TEST_PASSWORD", matches = ".+")
class ChatSendReceiveScenarioTest {

    @Test
    void 전송한_메시지를_같은_채팅방에서_수신한다() {
        ChatTestProperties properties = ChatTestProperties.fromEnvironment();
        ChatTestContext context = new ChatTestFixture(
            properties,
            WebtoonFixtureProperties.fromEnvironment()
        ).prepareChatRoom();
        String clientMessageId = UUID.randomUUID().toString();
        String testMessage = "chat-test-" + UUID.randomUUID();

        try (ChatTestClient client = new ChatTestClient(properties)) {
            client.connect(context.accessToken());
            client.subscribe(context.roomId());
            client.send(createRequest(context.roomId(), clientMessageId, testMessage));

            ChatMessageResponse receivedMessage = client.awaitMessage(
                message -> hasContent(message, testMessage),
                properties.timeout()
            );

            assertThat(receivedMessage).isNotNull();
            assertThat(receivedMessage.roomId()).isEqualTo(context.roomId());
            assertThat(receivedMessage.roomMemberId()).isEqualTo(context.roomMemberId());
            assertThat(receivedMessage.clientMessageId()).isEqualTo(clientMessageId);
            assertThat(receivedMessage.messageSequence()).isPositive();
        }
    }

    private ChatMessageRequest createRequest(
        long roomId,
        String clientMessageId,
        String content
    ) {
        MessageBlock messageBlock = new MessageBlock(
            MessageBlockType.TEXT,
            content,
            Map.of("source", "chat-test-client")
        );
        return new ChatMessageRequest(
            roomId, clientMessageId, List.of(messageBlock));
    }

    private boolean hasContent(ChatMessageResponse message, String expectedContent) {
        return message.messageBlocks() != null && message.messageBlocks().stream()
            .anyMatch(block -> expectedContent.equals(block.content()));
    }
}
