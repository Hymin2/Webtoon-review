package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.webtoon.entity.Comment;
import com.hymin.webtoon_review.webtoon.entity.CommentRecommend;

public class CommentRecommendMapper {

    public static CommentRecommend toCommentRecommend(User user, Comment comment) {
        return CommentRecommend.builder()
            .user(user)
            .comment(comment)
            .build();
    }
}
