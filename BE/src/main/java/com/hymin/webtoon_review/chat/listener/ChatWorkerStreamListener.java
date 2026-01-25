package com.hymin.webtoon_review.chat.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.dto.ChatMessageDispatchDto;
import com.hymin.webtoon_review.chat.dto.ChatMessageDto;
import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.repository.UserChatRoomRepository;
import com.hymin.webtoon_review.chat.repository.projection.ChatRoomParticipantGroups;
import com.hymin.webtoon_review.chat.service.ChatMessageSequenceGenerator;
import com.hymin.webtoon_review.chat.service.ChatSessionService;
import com.hymin.webtoon_review.global.constant.RedisGroupNames;
import com.hymin.webtoon_review.global.constant.RedisKeys;
import com.hymin.webtoon_review.global.constant.RedisStreamKeys;
import com.hymin.webtoon_review.global.constant.RedisTopicNames;
import com.hymin.webtoon_review.util.Time;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWorkerStreamListener implements
    StreamListener<String, MapRecord<String, String, String>> {

    @Value("${server.instance.name:default}")
    private String serverName;

    private final RedisTemplate<String, Long> longRedisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;

    private final ObjectMapper objectMapper;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageSequenceGenerator chatMessageSequenceGenerator;

    private final ChatSessionService chatSessionService;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        ChatMessageDto chatMessageDto = parseMessage(message);
        Long timestamp = message.getId().getTimestamp();
        ChatMessageResponse response = createChatMessageResponse(chatMessageDto, timestamp);

        cacheChatMessage(response, timestamp);
        log.info("[채팅 워커 서버 Listener 1/3] 채팅 메시지 캐싱 성공, {}", response.getMessageUUID());
        dispatchToServers(response);
        log.info("[채팅 워커 서버 Listener 2/3] Redis pub/sub을 통해 메시지 전파, {}", response.getMessageUUID());
        sendMessageToBatch(response);
        log.info("[채팅 워커 서버 Listener 3/4] 메시지 Batch 저장 서버에 메시지 발행, {}", response.getMessageUUID());
        acknowledgeMessage(message);
        log.info("[채팅 워커 서버 Listener 4/4] 메시지 ACK, {}", response.getMessageUUID());
    }

    private ChatMessageDto parseMessage(MapRecord<String, String, String> message) {
        try {
            return objectMapper.readValue(
                message.getValue().get("payload"),
                ChatMessageDto.class
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ChatMessageResponse createChatMessageResponse(
        ChatMessageDto chatMessageDto,
        long timestamp
    ) {
        Long messageSeq = chatMessageSequenceGenerator.generate(chatMessageDto.getRoomId());
        String createdAt = Time.toString(timestamp);

        return ChatMapper.toChatMessageResponse(
            chatMessageDto,
            messageSeq,
            createdAt
        );
    }

    private void cacheChatMessage(ChatMessageResponse chatMessageResponse, Long timestamp) {
        try {
            Long roomId = chatMessageResponse.getRoomId();
            String key = getRecentMessagesKey(roomId);
            String value = objectMapper.writeValueAsString(chatMessageResponse);

            stringRedisTemplate.opsForZSet().add(key, value, timestamp);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void dispatchToServers(ChatMessageResponse response) {
        Long roomId = response.getRoomId();

        Set<Long> participants = getParticipants(response.getRoomId());
        List<String> onlineMembers = new ArrayList<>(chatSessionService.getOnlineMembers(roomId));
        Set<Long> offlineMembers = new HashSet<>(participants);
        List<String> sessionKeys = new ArrayList<>();

        if (onlineMembers.isEmpty()) {
            return;
        }

        onlineMembers.forEach(member -> {
            Long userId = Long.valueOf(member.split("_")[0]);
            String clientId = member.split("_")[1];

            offlineMembers.remove(userId);
            sessionKeys.add(
                chatSessionService.getUserChatSessionKey(userId, clientId)
            );
        });

        List<String> serverNames = stringRedisTemplate.opsForValue().multiGet(sessionKeys);

        Map<String, List<String>> userServerMap = new HashMap<>();
        for (int i = 0; i < onlineMembers.size(); i++) {
            String serverName = serverNames.get(i);
            if (!userServerMap.containsKey(serverName)) {
                userServerMap.put(serverName, new ArrayList<>());
            }

            if (serverName != null) {
                List<String> userIds = userServerMap.get(serverName);
                userIds.add(onlineMembers.get(i));
            }
        }

        serverNames.forEach(serverName -> {
            ChatMessageDispatchDto chatMessageDispatchDto = ChatMessageDispatchDto.builder()
                .chatMessageResponse(response)
                .userIds(userServerMap.get(serverName))
                .build();

            String topicName = getTopicName(serverName);
            objectRedisTemplate.convertAndSend(topicName, chatMessageDispatchDto);
        });
    }

    private void sendMessageToBatch(ChatMessageResponse response) {
        try {
            ObjectRecord<String, String> record = StreamRecords.newRecord()
                .in(RedisStreamKeys.CHAT_MESSAGE_BATCH)
                .ofObject(objectMapper.writeValueAsString(ChatMapper.toChatMessage(response)));

            stringRedisTemplate.opsForStream().add(record);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void acknowledgeMessage(MapRecord<String, String, String> message) {
        String streamKey = message.getStream();
        String groupName = RedisGroupNames.CHAT_WORKER_GROUP_NAME;

        stringRedisTemplate.opsForStream().acknowledge(streamKey, groupName, message.getId());
    }

    private Set<Long> getParticipants(Long roomId) {
        String key = getChatRoomMembersKey(roomId);

        if (!longRedisTemplate.hasKey(key)) {
            Set<Long> participants = userChatRoomRepository.findParticipantsByChatRoomId(roomId)
                .stream().map(ChatRoomParticipantGroups::getUserId)
                .collect(Collectors.toSet());
            longRedisTemplate.opsForSet().add(key, participants.toArray(new Long[0]));
            longRedisTemplate.expire(key, Duration.ofHours(2L));

            return participants;
        } else {
            return longRedisTemplate.opsForSet().members(key);
        }
    }

    private String getTopicName(String serverName) {
        return RedisTopicNames.CHAT_MESSAGE_PREFIX + serverName
            + RedisTopicNames.CHAT_MESSAGE_POSTFIX;
    }

    private String getRecentMessagesKey(Long roomId) {
        return RedisKeys.CHAT_ROOM_PREFIX + roomId + RedisKeys.CHAT_ROOM_RECENT_MESSAGES_POSTFIX;
    }

    private String getChatRoomMembersKey(Long roomId) {
        return RedisKeys.CHAT_ROOM_PREFIX + roomId + RedisKeys.CHAT_ROOM_MEMBERS_POSTFIX;
    }
}
