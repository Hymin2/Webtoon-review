package com.hymin.webtoon_review.chat.repository;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.util.Time;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class ChatMessageCustomRepositoryImpl implements ChatMessageCustomRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveAll(List<ChatMessage> chatMessages) {
        String sql = "INSERT INTO message(user_id, chat_room_id, type, content, created_at, updated_at) VALUES ((SELECT id FROM user WHERE nickname = ?),?,?,?,?,?)";

        jdbcTemplate.batchUpdate(sql, chatMessages, chatMessages.size(), (ps, message) -> {
            ps.setString(1, message.getSender());
            ps.setLong(2, message.getRoomId());
            ps.setString(3, message.getType().name());
            ps.setString(4, message.getMessage());
            ps.setString(5, Time.now());
            ps.setString(6, Time.now());
        });
    }
}
