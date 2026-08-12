package com.hymin.webtoon_review.chat.common.service;

import com.hymin.webtoon_review.global.constant.RedisKeys;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Long> longRedisTemplate;

    public void addUserChatSession(Long userId, String clientId, String serverName) {
        String key = getUserChatSessionKey(userId, clientId);
        redisTemplate.opsForValue().set(key, serverName);
    }

    public void removeUserChatSession(Long userId, String clientId) {
        String key = getUserChatSessionKey(userId, clientId);
        redisTemplate.delete(key);
    }

    public void cleanupUserChatSession(String serverName) {
        ScanOptions options = ScanOptions.scanOptions().count(100).build();
        String connectedUsersKey = getServerConnectedUserKey(serverName);

        try (Cursor<String> userCursor = redisTemplate.opsForSet()
            .scan(connectedUsersKey, options)) {
            List<String> chunk = new ArrayList<>();

            while (userCursor.hasNext()) {
                chunk.add(userCursor.next());

                if (chunk.size() == 100 || !userCursor.hasNext()) {
                    executeCleanupPipeline(chunk);
                    chunk.clear();
                }
            }
        }

        redisTemplate.delete(connectedUsersKey);
    }

    public void addServerConnectedUser(Long userId, String clientId, String serverName) {
        String key = getServerConnectedUserKey(serverName);
        String value = userId + "_" + clientId;

        redisTemplate.opsForSet().add(key, value);
    }

    public void removeServerConnectedUser(Long userId, String clientId, String serverName) {
        String key = getServerConnectedUserKey(serverName);
        String value = userId + "_" + clientId;

        redisTemplate.opsForSet().remove(key, value);
    }

    public Set<String> getOnlineMembers(Long roomId) {
        String key = getOnlineMembersKey(roomId);
        return redisTemplate.opsForSet().members(key);
    }

    public void addChatRoomOnlineMember(Long roomId, Long userId, String clientId) {
        String value = userId + "_" + clientId;

        redisTemplate.opsForSet().add(getOnlineMembersKey(roomId), value);
        longRedisTemplate.opsForSet().add(getUserJoinedRoomKey(userId, clientId), roomId);
    }

    public void removeChatRoomOnlineMember(Long roomId, Long userId, String clientId) {
        String value = userId + "_" + clientId;

        redisTemplate.opsForSet().remove(getOnlineMembersKey(roomId), value);
        longRedisTemplate.opsForSet().remove(getUserJoinedRoomKey(userId, clientId), roomId);
    }

    public String getServerConnectedUserKey(String serverName) {
        return RedisKeys.CHAT_SERVER_PREFIX + serverName
            + RedisKeys.CHAT_SERVER_CONNECTED_USER_POSTFIX;
    }

    public String getOnlineMembersKey(Long roomId) {
        return RedisKeys.CHAT_ROOM_PREFIX + roomId + RedisKeys.CHAT_ROOM_ONLINE_MEMBERS_POSTFIX;
    }

    public String getUserChatSessionKey(Long userId, String clientId) {
        return RedisKeys.USER_PREFIX + userId + RedisKeys.USER_CHAT_SESSION_PREFIX + clientId;
    }

    public String getUserJoinedRoomKey(Long userId, String clientId) {
        return RedisKeys.USER_PREFIX + userId + RedisKeys.USER_CHAT_JOINED_ROOM_PREFIX + clientId;
    }

    private void executeCleanupPipeline(List<String> chunk) {
        List<Object> roomList = redisTemplate.executePipelined(
            (RedisCallback<Object>) connection -> {
                chunk.forEach(u -> {
                    Long userId = Long.valueOf(u.split("_")[0]);
                    String clientId = u.split("_")[1];

                    connection.setCommands()
                        .sMembers(getUserJoinedRoomKey(userId, clientId).getBytes());
                });
                return null;
            });

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (int i = 0; i < chunk.size(); i++) {
                String userIdClientId = chunk.get(i);
                Long userId = Long.valueOf(userIdClientId.split("_")[0]);
                String clientId = userIdClientId.split("_")[1];

                Set<String> roomIds = (Set<String>) roomList.get(i);

                if (roomIds != null) {
                    roomIds.forEach(roomId ->
                        connection.setCommands()
                            .sRem(getOnlineMembersKey(Long.valueOf(roomId)).getBytes(),
                                userIdClientId.getBytes())
                    );
                }

                connection.keyCommands().del(getUserChatSessionKey(userId, clientId).getBytes());
                connection.keyCommands().del(getUserJoinedRoomKey(userId, clientId).getBytes());
            }
            return null;
        });
    }
}
