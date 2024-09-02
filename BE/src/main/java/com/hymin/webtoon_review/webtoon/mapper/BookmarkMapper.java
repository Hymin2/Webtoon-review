package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.user.entity.Bookmark;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;

public class BookmarkMapper {

    public static Bookmark toBookmark(User user, Webtoon webtoon) {
        return Bookmark.builder()
            .user(user)
            .webtoon(webtoon)
            .build();
    }
}
