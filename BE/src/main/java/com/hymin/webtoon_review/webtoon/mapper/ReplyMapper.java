package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.ReplyInfo;
import com.hymin.webtoon_review.webtoon.entity.Comment;
import com.hymin.webtoon_review.webtoon.entity.Reply;

public class ReplyMapper {

    public static Reply toReply(User user, Comment comment, ReplyInfo replyInfo) {
        return Reply.builder()
            .user(user)
            .comment(comment)
            .content(replyInfo.getContent())
            .status(Boolean.TRUE)
            .build();
    }
}
