package com.hymin.webtoon_review.chat.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserChatRoomTest {

    @Test
    void roomMemberId가_없으면_저장_전에_생성한다() {
        UserChatRoom userChatRoom = UserChatRoom.builder().build();

        userChatRoom.assignRoomMemberId();

        assertThat(userChatRoom.getRoomMemberId()).isNotBlank();
    }

    @Test
    void 기존_roomMemberId는_변경하지_않는다() {
        String roomMemberId = "room-member-id";
        UserChatRoom userChatRoom = UserChatRoom.builder()
            .roomMemberId(roomMemberId)
            .build();

        userChatRoom.assignRoomMemberId();

        assertThat(userChatRoom.getRoomMemberId()).isEqualTo(roomMemberId);
    }
}
