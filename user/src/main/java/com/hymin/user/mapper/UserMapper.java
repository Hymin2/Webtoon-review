package com.hymin.user.mapper;

import com.hymin.common.utils.PrimaryKeyGenerator;
import com.hymin.user.dto.UserRegister;
import com.hymin.user.entity.Authority;
import com.hymin.user.entity.User;

public class UserMapper {

    public static User toUser(PrimaryKeyGenerator primaryKeyGenerator, UserRegister userRegister) {
        return User.builder()
            .id(primaryKeyGenerator.generate())
            .username(userRegister.getUsername())
            .password(userRegister.getPassword())
            .nickname(userRegister.getNickname())
            .build();
    }

    public static Authority toUserAuthority(PrimaryKeyGenerator primaryKeyGenerator, User user) {
        return Authority.builder()
            .id(primaryKeyGenerator.generate())
            .name("ROLE_USER")
            .user(user)
            .build();
    }
}
