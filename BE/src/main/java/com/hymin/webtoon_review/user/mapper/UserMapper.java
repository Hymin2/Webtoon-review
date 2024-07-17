package com.hymin.webtoon_review.user.mapper;

import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.entity.User;

public class UserMapper {

    public static User toUser(RegisterInfo registerInfo, String encodedPassword) {
        return User.builder()
            .username(registerInfo.getUsername())
            .password(encodedPassword)
            .nickname(registerInfo.getNickname())
            .build();
    }
}
