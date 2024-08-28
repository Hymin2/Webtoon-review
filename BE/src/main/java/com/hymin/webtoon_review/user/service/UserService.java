package com.hymin.webtoon_review.user.service;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.global.security.JwtService;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.entity.Authority;
import com.hymin.webtoon_review.user.entity.Bookmark;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.entity.WebtoonRecommend;
import com.hymin.webtoon_review.user.exception.AlreadyUserExistsException;
import com.hymin.webtoon_review.user.exception.BookmarkNotFoundException;
import com.hymin.webtoon_review.user.exception.RecommendationNotFoundException;
import com.hymin.webtoon_review.user.exception.UserNotFoundException;
import com.hymin.webtoon_review.user.mapper.UserMapper;
import com.hymin.webtoon_review.user.repository.AuthorityRepository;
import com.hymin.webtoon_review.user.repository.BookmarkRepository;
import com.hymin.webtoon_review.user.repository.UserRepository;
import com.hymin.webtoon_review.user.repository.WebtoonRecommendRepository;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final WebtoonService webtoonService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final BookmarkRepository bookmarkRepository;
    private final WebtoonRecommendRepository webtoonRecommendRepository;
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

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(ResponseStatus.USER_NOT_FOUND));
    }

    @Transactional
    public void addBookmark(Authentication authentication, Long webtoonId) {
        User user = getUserByUsername(authentication.getName());
        Webtoon webtoon = webtoonService.getWebtoon(webtoonId);

        Bookmark bookmark = Bookmark.builder()
            .user(user)
            .webtoon(webtoon)
            .build();

        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(Authentication authentication, Long bookmarkId) {
        User user = getUserByUsername(authentication.getName());
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
            .orElseThrow(() -> new BookmarkNotFoundException(ResponseStatus.BOOKMARK_NOT_FOUND));

        if (!bookmark.getUser().equals(user)) {
            throw new GeneralException(ResponseStatus.BAD_REQUEST);
        }

        bookmarkRepository.delete(bookmark);
    }

    @Transactional
    public void addRecommendation(Authentication authentication, Long webtoonId) {
        User user = getUserByUsername(authentication.getName());
        Webtoon webtoon = webtoonService.getWebtoon(webtoonId);

        WebtoonRecommend webtoonRecommend = WebtoonRecommend.builder()
            .user(user)
            .webtoon(webtoon)
            .build();

        webtoonRecommendRepository.save(webtoonRecommend);
    }

    @Transactional
    public void removeRecommendation(Authentication authentication, Long recommendId) {
        User user = getUserByUsername(authentication.getName());
        WebtoonRecommend webtoonRecommend = webtoonRecommendRepository.findById(recommendId)
            .orElseThrow(
                () -> new RecommendationNotFoundException(ResponseStatus.RECOMMENDATION_NOT_FOUND));

        if (!webtoonRecommend.getUser().equals(user)) {
            throw new GeneralException(ResponseStatus.BAD_REQUEST);
        }

        webtoonRecommendRepository.delete(webtoonRecommend);
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
