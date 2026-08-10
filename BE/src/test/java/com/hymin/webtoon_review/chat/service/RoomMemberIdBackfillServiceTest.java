package com.hymin.webtoon_review.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.chat.repository.UserChatRoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoomMemberIdBackfillServiceTest {

    @Mock
    private UserChatRoomRepository userChatRoomRepository;

    @InjectMocks
    private RoomMemberIdBackfillService roomMemberIdBackfillService;

    @Test
    void roomMemberId가_없는_기존_행을_보정한다() {
        when(userChatRoomRepository.assignMissingRoomMemberIds()).thenReturn(2);

        int updatedCount = roomMemberIdBackfillService.backfill();

        assertThat(updatedCount).isEqualTo(2);
        verify(userChatRoomRepository).assignMissingRoomMemberIds();
    }
}
