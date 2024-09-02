package com.hymin.webtoon_review.user.facade;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.global.security.JwtService;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.entity.Authority;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.exception.AlreadyUserExistsException;
import com.hymin.webtoon_review.user.exception.UserNotFoundException;
import com.hymin.webtoon_review.user.mapper.UserMapper;
import com.hymin.webtoon_review.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterInfo registerInfo) {
        if (userService.existsByUsernameOrNickname(registerInfo.getUsername(),
            registerInfo.getNickname())) {
            throw new AlreadyUserExistsException(ResponseStatus.ALREADY_USER_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(registerInfo.getPassword());
        User user = UserMapper.toUser(registerInfo, encodedPassword);

        Authority authority = createUserAuthority(user);
        user.setAuthorities(List.of(authority));

        userService.save(user, authority);
    }

    public Boolean checkDuplicatedUsername(String username) {
        return userService.existsByUsername(username);
    }

    public Boolean checkDuplicatedNickname(String nickname) {
        return userService.existsByNickname(nickname);
    }

    public String login(Authentication authentication) {
        if (!authentication.isAuthenticated()) {
            throw new UserNotFoundException(ResponseStatus.LOGIN_FAILED);
        }

        String jwt = jwtService.createJwt(authentication);

        return "Bearer " + jwt;
    }

    private Authority createUserAuthority(User user) {
        return Authority.builder()
            .name("ROLE_USER")
            .user(user)
            .build();
    }
}
