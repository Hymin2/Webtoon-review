package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.CommentInfo;
import com.hymin.webtoon_review.webtoon.entity.Comment;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;

public class CommentMapper {

    public static Comment toComment(User user, Webtoon webtoon, CommentInfo commentInfo) {
        return Comment.builder()
            .user(user)
            .webtoon(webtoon)
            .content(commentInfo.getComment())
            .score(commentInfo.getScore())
            .build();
    }
}
