package com.hymin.webtoon_review.chat.connection.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.common.dto.ChatMessageDispatchDto;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.global.manager.TraceContextManager;
import com.hymin.webtoon_review.global.manager.TraceContextManager.TraceScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("chat")
@RequiredArgsConstructor
public class ChatMessageListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final TraceContextManager traceContextManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        ChatMessageDispatchDto dto = parseMessage(message);
        try (TraceScope scope = traceContextManager.setExternalTraceId(dto.getTraceId(),
                "chat-message-send")) {
            traceContextManager.putChatMDC(
                    dto.getChatMessageResponse().getRoomId(),
                    dto.getSenderId()
            );

            log.info("[채팅 서버 Listener 1/2] 전파된 메시지 수신 완료");
            sendMessage(dto);
            log.info("[채팅 서버 Listener 2/2] 메시지 최종 전달");
        } finally {
            traceContextManager.removeChatMDC();
        }
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
