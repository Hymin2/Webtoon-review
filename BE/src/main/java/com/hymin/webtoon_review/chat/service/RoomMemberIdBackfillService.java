package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.chat.repository.UserChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomMemberIdBackfillService {

    private final UserChatRoomRepository userChatRoomRepository;

    @Transactional
    public int backfill() {
        return userChatRoomRepository.assignMissingRoomMemberIds();
    }
}
