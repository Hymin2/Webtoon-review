package com.hymin.chattest.fixture;

import com.hymin.chattest.client.ChatTestLoginClient;
import com.hymin.chattest.support.ChatTestProperties;
import java.util.Arrays;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class ChatTestFixture {

    private final ChatTestProperties properties;
    private final WebtoonFixtureProperties webtoonProperties;
    private final RestClient restClient;

    public ChatTestFixture(
        ChatTestProperties properties,
        WebtoonFixtureProperties webtoonProperties
    ) {
        this.properties = properties;
        this.webtoonProperties = webtoonProperties;
        this.restClient = RestClient.builder()
            .baseUrl(properties.httpUrl().toString())
            .build();
    }

    public String prepareUser() {
        if (!userExists()) {
            registerUser();
        }
        return new ChatTestLoginClient(properties).login();
    }

    public ChatTestContext prepareChatRoom() {
        long webtoonId = new WebtoonFixture(webtoonProperties).ensureWebtoon();
        String accessToken = prepareUser();
        Long roomId = findRoomId(accessToken);

        if (roomId == null) {
            joinChatRoom(accessToken, webtoonId);
            roomId = findRoomId(accessToken);
        }
        if (roomId == null) {
            throw new IllegalStateException("테스트 채팅방 ID를 확인할 수 없습니다.");
        }
        return new ChatTestContext(accessToken, roomId);
    }

    private boolean userExists() {
        BooleanResponse response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/users/check/username")
                .queryParam("username", properties.username())
                .build())
            .retrieve()
            .body(BooleanResponse.class);

        return response != null && Boolean.TRUE.equals(response.data());
    }

    private void registerUser() {
        restClient.post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new RegisterRequest(
                properties.username(),
                properties.password(),
                properties.username(),
                "MALE"
            ))
            .retrieve()
            .toBodilessEntity();
    }

    private Long findRoomId(String accessToken) {
        ChatRoomListResponse response = restClient.get()
            .uri("/chat/room")
            .header("Authorization", authorizationHeader(accessToken))
            .retrieve()
            .body(ChatRoomListResponse.class);

        if (response == null || response.data() == null) {
            return null;
        }
        return Arrays.stream(response.data())
            .filter(room -> webtoonProperties.webtoonName().equals(room.roomName()))
            .map(ChatRoom::roomId)
            .findFirst()
            .orElse(null);
    }

    private void joinChatRoom(String accessToken, long webtoonId) {
        restClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/chat/room")
                .queryParam("webtoonId", webtoonId)
                .build())
            .header("Authorization", authorizationHeader(accessToken))
            .retrieve()
            .toBodilessEntity();
    }

    private String authorizationHeader(String accessToken) {
        return accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
    }

    private record RegisterRequest(
        String username,
        String password,
        String nickname,
        String gender
    ) {
    }

    private record BooleanResponse(Boolean data) {
    }

    private record ChatRoomListResponse(ChatRoom[] data) {
    }

    private record ChatRoom(Long roomId, String roomName) {
    }
}
