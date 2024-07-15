package com.hymin.webtoon_review.user.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.dto.UserRequest.LoginInfo;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.dto.UserResponse.LoginResult;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.exception.UserNotFoundException;
import com.hymin.webtoon_review.user.mapper.UserMapper;
import com.hymin.webtoon_review.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void register(RegisterInfo registerInfo) {
        User user = UserMapper.toUser(registerInfo);

        userRepository.save(user);
    }

    public LoginResult login(LoginInfo loginInfo) {
        if (!userRepository.existsByUsernameAndPassword(loginInfo.getUsername(),
            loginInfo.getPassword())) {
            throw new UserNotFoundException(ResponseStatus.USER_NOT_FOUND);
        }

        String sessionId = UUID.randomUUID().toString();

        return UserMapper.toLoginResult(sessionId);
    }
}
