package com.hymin.webtoon_review.chat.server.facade;

import com.hymin.webtoon_review.chat.common.dto.ChatRequest.ChatMessageRequest;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.common.service.ChatService;
import com.hymin.webtoon_review.chat.server.service.*;
import com.hymin.webtoon_review.global.manager.TraceContextManager;
import com.hymin.webtoon_review.global.manager.TraceContextManager.TraceScope;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("chat")
@RequiredArgsConstructor
public class ChatFacade {
    private final TraceContextManager traceManager;
    private final ChatService chatService;
    private final ChatMessageRoutingService routingService;
    private final ChatMessageQueryService queryService;
    private final ChatServerMessageIdService messageIdService;

    public void sendMessage(ChatMessageRequest request, Long userId, String nickname) {
        try (TraceScope scope = traceManager.startNewSpan("chat-message-receive")) {
            String traceId = scope.span().context().traceId();
            traceManager.putChatMDC(request.getRoomId(), userId);
            log.info("[채팅] 채팅 메시지 수신 완료");
            String memberId = chatService.getRoomMemberId(userId, request.getRoomId());
            String messageId = messageIdService.getOrCreate(request.getRoomId(), userId,
                    request.getClientMessageId());
            routingService.route(ChatMapper.toChatMessageDto(request, userId, nickname,
                    memberId, traceId, messageId));
        } finally { traceManager.removeChatMDC(); }
    }

    public List<ChatMessageResponse> getMessagesAfter(Long roomId, Long sequence, Long userId) {
        chatService.getRoomMemberId(userId, roomId);
        return queryService.getMessagesAfter(roomId, sequence);
    }
}
