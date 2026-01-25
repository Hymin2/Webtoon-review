package com.hymin.webtoon_review.chat.listener;

import com.hymin.webtoon_review.chat.route.ChatWorkerLocalHashRing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWorkerEvenetMessageListener implements MessageListener {

    private final ChatWorkerLocalHashRing chatWorkerLocalHashRing;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("[채팅 서버] 채팅 워커 서버의 변동이 감지되어 Local Hash Ring을 Refresh");
        chatWorkerLocalHashRing.refresh();
    }
}
