package com.hymin.webtoon_review.user.mapper;

import com.hymin.webtoon_review.user.dto.UserRequest.DeviceRequest;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.dto.UserResponse.TokenResponse;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.entity.UserDevice;

public class UserMapper {

    public static User toUser(RegisterInfo registerInfo, String encodedPassword) {
        return User.builder()
            .username(registerInfo.getUsername())
            .password(encodedPassword)
            .nickname(registerInfo.getNickname())
            .gender(registerInfo.getGender())
            .build();
    }

    public static TokenResponse toTokenResponse(String refreshToken, String accessToken) {
        return TokenResponse.builder()
            .refreshToken(refreshToken)
            .accessToken(accessToken)
            .build();
    }

    public static UserDevice toUserDevice(User user, DeviceRequest deviceRequest) {
        return UserDevice.builder()
            .Type(deviceRequest.getType())
            .deviceId(deviceRequest.getDeviceId())
            .pushToken(deviceRequest.getPushToken())
            .user(user)
            .build();
    }
}
