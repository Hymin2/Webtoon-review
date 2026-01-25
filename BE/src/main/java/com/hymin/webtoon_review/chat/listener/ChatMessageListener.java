package com.hymin.webtoon_review.chat.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.dto.ChatMessageDispatchDto;
import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        ChatMessageDispatchDto chatMessageDispatchDto = parseMessage(message);
        String messageUUID = chatMessageDispatchDto.getChatMessageResponse().getMessageUUID();
        log.info("[채팅 서버 Listener 1/2] 전파된 메시지 수신 완료, {}", messageUUID);
        sendMessage(chatMessageDispatchDto);
        log.info("[채팅 서버 Listener 2/2] 메시지 최종 전달, {}", messageUUID);
    }

    private ChatMessageDispatchDto parseMessage(Message message) {
        try {
            return objectMapper.readValue(
                redisTemplate.getStringSerializer().deserialize(message.getBody()),
                ChatMessageDispatchDto.class
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(ChatMessageDispatchDto chatMessageDispatchDto) {
        ChatMessageResponse chatMessageResponse = chatMessageDispatchDto.getChatMessageResponse();
        String destination = "/queue/room/" + chatMessageResponse.getRoomId();

        chatMessageDispatchDto.getUserIds().forEach(
            userId -> simpMessagingTemplate.convertAndSendToUser(userId, destination,
                chatMessageResponse)
        );
    }
}
