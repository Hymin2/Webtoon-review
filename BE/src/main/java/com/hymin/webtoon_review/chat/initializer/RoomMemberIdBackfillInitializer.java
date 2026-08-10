package com.hymin.webtoon_review.chat.initializer;

import com.hymin.webtoon_review.chat.service.RoomMemberIdBackfillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("chat")
@RequiredArgsConstructor
public class RoomMemberIdBackfillInitializer {

    private final RoomMemberIdBackfillService roomMemberIdBackfillService;

    @EventListener(ApplicationReadyEvent.class)
    public void backfill() {
        int updatedCount = roomMemberIdBackfillService.backfill();
        log.info("[채팅] roomMemberId 보정 완료: updatedCount={}", updatedCount);
    }
}
