package com.hymin.webtoon_review.user.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.global.security.JwtService;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.entity.Authority;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.exception.AlreadyUserExistsException;
import com.hymin.webtoon_review.user.exception.UserNotFoundException;
import com.hymin.webtoon_review.user.mapper.UserMapper;
import com.hymin.webtoon_review.user.repository.AuthorityRepository;
import com.hymin.webtoon_review.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterInfo registerInfo) {
        if (userRepository.existsByUsernameOrNickname(registerInfo.getUsername(),
            registerInfo.getNickname())) {
            throw new AlreadyUserExistsException(ResponseStatus.ALREADY_USER_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(registerInfo.getPassword());
        User user = UserMapper.toUser(registerInfo, encodedPassword);
        userRepository.save(user);

        Authority authority = createUserAuthority(user);
        user.setAuthorities(List.of(authority));
    }

    public String login(Authentication authentication) {
        if (!authentication.isAuthenticated()) {
            throw new UserNotFoundException(ResponseStatus.LOGIN_FAILED);
        }

        String jwt = jwtService.createJwt(authentication);

        return "Bearer " + jwt;
    }

    public Boolean checkDuplicatedUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Boolean checkDuplicatedNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    private Authority createUserAuthority(User user) {
        Authority authority = Authority.builder()
            .name("ROLE_USER")
            .user(user)
            .build();

        authorityRepository.save(authority);

        return authority;
    }
}
