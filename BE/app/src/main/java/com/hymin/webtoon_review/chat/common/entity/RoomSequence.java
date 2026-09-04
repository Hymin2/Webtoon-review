package com.hymin.webtoon_review.chat.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "room_sequence")
public class RoomSequence {

    @Id
    private Long roomId;
    private long sequence;
}
