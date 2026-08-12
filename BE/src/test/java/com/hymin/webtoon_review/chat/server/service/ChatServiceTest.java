package com.hymin.webtoon_review.chat.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.chat.common.ChatRoomRepository;
import com.hymin.webtoon_review.chat.common.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.common.UserChatRoomRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private UserChatRoomRepository userChatRoomRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void 채팅방_회원의_roomMemberId를_조회한다() {
        when(userChatRoomRepository.findRoomMemberId(1L, 2L))
            .thenReturn(Optional.of("room-member-id"));

        assertThat(chatService.getRoomMemberId(1L, 2L)).isEqualTo("room-member-id");
    }

    @Test
    void 채팅방_회원이_아니면_메시지를_전송할_수_없다() {
        when(userChatRoomRepository.findRoomMemberId(1L, 2L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getRoomMemberId(1L, 2L))
            .isInstanceOf(InvalidChatRoomAccessException.class);
    }
}
