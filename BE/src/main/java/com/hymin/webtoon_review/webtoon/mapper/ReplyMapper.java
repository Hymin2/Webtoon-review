package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.webtoon.entity.Comment;
import com.hymin.webtoon_review.webtoon.entity.Reply;

public class ReplyMapper {

    public static Reply toReply(User user, Comment comment) {
        return Reply.builder()
            .user(user)
            .comment(comment)
            .build();
    }
}
