package com.hymin.webtoon_review.user.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.entity.Authority;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.exception.UserNotFoundException;
import com.hymin.webtoon_review.user.repository.AuthorityRepository;
import com.hymin.webtoon_review.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    public User get(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(ResponseStatus.USER_NOT_FOUND));
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public Boolean existsByUsernameOrNickname(String username, String nickname) {
        return userRepository.existsByUsernameOrNickname(username, nickname);
    }

    public void save(User user, Authority authority) {
        authorityRepository.save(authority);
        userRepository.save(user);
    }
}
