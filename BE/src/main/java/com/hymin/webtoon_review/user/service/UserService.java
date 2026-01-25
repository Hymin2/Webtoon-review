package com.hymin.webtoon_review.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.global.constant.RedisKeys;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.dto.UserTokenDto;
import com.hymin.webtoon_review.user.entity.Authority;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.entity.UserDevice;
import com.hymin.webtoon_review.user.exception.UserNotFoundException;
import com.hymin.webtoon_review.user.repository.AuthorityRepository;
import com.hymin.webtoon_review.user.repository.UserDeviceRepository;
import com.hymin.webtoon_review.user.repository.UserRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final RedisTemplate<String, String> redisTemplate;

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

    public UserTokenDto getRefreshToken(Long userId, String deviceType, String clientId) {
        try {
            String key = getRefreshTokenKey(userId, deviceType, clientId);
            return objectMapper.readValue(
                redisTemplate.opsForValue().get(key),
                UserTokenDto.class
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveRefreshToken(Long userId, String deviceType, String clientId,
        UserTokenDto userTokenDto) {
        try {
            String key = getRefreshTokenKey(userId, deviceType, clientId);
            String value = objectMapper.writeValueAsString(userTokenDto);
            redisTemplate.opsForValue().set(key, value, REFRESH_TOKEN_DURATION);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRefreshToken(Long userId, String deviceType, String clientId) {
        String key = getRefreshTokenKey(userId, deviceType, clientId);
        redisTemplate.delete(key);
    }

    public void addBlacklist(String accessToken, Duration ttl) {
        String key = RedisKeys.ACCESS_TOKEN_BLACKLISTS_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "1", ttl);
    }

    public boolean isBlacklisted(String accessToken) {
        String key = RedisKeys.ACCESS_TOKEN_BLACKLISTS_PREFIX + accessToken;
        return redisTemplate.hasKey(key);
    }

    public void saveDevice(UserDevice userDevice) {
        userDeviceRepository.save(userDevice);
    }

    private String getRefreshTokenKey(Long userId, String deviceType, String clientId) {
        return String.format("%s%d%s:%s_%s",
            RedisKeys.USER_PREFIX,
            userId,
            RedisKeys.USER_REFRESH_TOKEN_POSTFIX,
            deviceType,
            clientId
        );
    }
}
