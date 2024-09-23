package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.webtoon.entity.Reply;
import com.hymin.webtoon_review.webtoon.entity.ReplyRecommend;

public class ReplyRecommendMapper {

    public static ReplyRecommend toReplyRecommend(User user, Reply reply) {
        return ReplyRecommend.builder()
            .user(user)
            .reply(reply)
            .build();
    }
}
