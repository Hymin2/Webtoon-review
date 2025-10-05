package com.hymin.webtoon_review.global.inspector;

import com.hymin.webtoon_review.chat.repository.UnacknowledgedMessageRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final UnacknowledgedMessageRepository unacknowledgedMessageRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
            StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        Principal principal = accessor.getUser();

        if (principal == null) {
            return message;
        }

        String username = principal.getName();

        if (command == StompCommand.CONNECT) {
            unacknowledgedMessageRepository.connectUser(username);
        } else if (command == StompCommand.DISCONNECT) {
            unacknowledgedMessageRepository.disconnectUser(username);
        }

        return message;
    }
}
