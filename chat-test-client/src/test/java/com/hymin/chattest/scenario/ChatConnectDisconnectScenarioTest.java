package com.hymin.chattest.scenario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.hymin.chattest.client.ChatTestClient;
import com.hymin.chattest.fixture.ChatTestFixture;
import com.hymin.chattest.fixture.WebtoonFixtureProperties;
import com.hymin.chattest.support.ChatTestProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@Tag("chat-basic")
@EnabledIfEnvironmentVariable(named = "CHAT_TEST_USERNAME", matches = ".+")
@EnabledIfEnvironmentVariable(named = "CHAT_TEST_PASSWORD", matches = ".+")
class ChatConnectDisconnectScenarioTest {

    @Test
    void 채팅방을_구독하지_않고_연결을_종료한다() {
        ChatTestProperties properties = ChatTestProperties.fromEnvironment();
        String accessToken = new ChatTestFixture(
            properties,
            WebtoonFixtureProperties.fromEnvironment()
        ).prepareUser();
        ChatTestClient client = new ChatTestClient(properties);

        try {
            client.connect(accessToken);
            assertThat(client.isConnected()).isTrue();

            assertThatCode(client::close).doesNotThrowAnyException();

            assertThat(client.isConnected()).isFalse();
            assertThat(client.connectionFailure()).isNull();
        } finally {
            if (client.isConnected()) {
                client.close();
            }
        }
    }
}
