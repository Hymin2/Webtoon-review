package com.hymin.webtoon_review.user.mapper;

import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.dto.UserResponse.LoginResult;
import com.hymin.webtoon_review.user.entity.User;

public class UserMapper {

    public static User toUser(RegisterInfo registerInfo) {
        return User.builder()
            .username(registerInfo.getUsername())
            .password(registerInfo.getPassword())
            .nickname(registerInfo.getNickname())
            .build();
    }

    public static LoginResult toLoginResult(String sessionId) {
        return LoginResult.builder()
            .sessionId(sessionId)
            .build();
    }
}
