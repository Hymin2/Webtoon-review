package com.hymin.webtoon_review.chat.client;

import com.hymin.webtoon_review.chat.support.ChatTestProperties;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class ChatTestLoginClient {

    private final ChatTestProperties properties;
    private final RestClient restClient;

    public ChatTestLoginClient(ChatTestProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
            .baseUrl(properties.httpUrl().toString())
            .build();
    }

    public String login() {
        LoginResponse response = restClient.post()
            .uri("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Device-Type", properties.deviceType())
            .header("X-Client-Id", properties.clientId())
            .body(new LoginRequest(properties.username(), properties.password()))
            .retrieve()
            .body(LoginResponse.class);

        if (response == null || response.data() == null
            || response.data().accessToken() == null || response.data().accessToken().isBlank()) {
            throw new IllegalStateException("로그인 응답에 accessToken이 없습니다.");
        }
        return response.data().accessToken();
    }

    private record LoginRequest(String username, String password) {
    }

    private record LoginResponse(TokenData data) {
    }

    private record TokenData(String accessToken) {
    }
}
