package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.entity.WebtoonRecommend;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;

public class RecommendMapper {

    public static WebtoonRecommend toWebtoonRecommend(User user, Webtoon webtoon) {
        return WebtoonRecommend.builder()
            .user(user)
            .webtoon(webtoon)
            .build();
    }
}
