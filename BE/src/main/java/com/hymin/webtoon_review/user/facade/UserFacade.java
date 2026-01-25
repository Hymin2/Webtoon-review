package com.hymin.webtoon_review.user.facade;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.global.security.JwtService;
import com.hymin.webtoon_review.global.security.UserDetailsImpl;
import com.hymin.webtoon_review.user.dto.UserRequest.DeviceRequest;
import com.hymin.webtoon_review.user.dto.UserRequest.RefreshRequest;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.dto.UserResponse.TokenResponse;
import com.hymin.webtoon_review.user.dto.UserTokenDto;
import com.hymin.webtoon_review.user.entity.Authority;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.exception.AlreadyUserExistsException;
import com.hymin.webtoon_review.user.exception.InvalidRefreshTokenException;
import com.hymin.webtoon_review.user.exception.RefreshTokenReuseException;
import com.hymin.webtoon_review.user.exception.UserNotFoundException;
import com.hymin.webtoon_review.user.mapper.UserMapper;
import com.hymin.webtoon_review.user.service.UserService;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final long ACCESS_VALID_SECOND = 1000L * 60 * 60 * 24 * 30;

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

    @Transactional(readOnly = true)
    public Boolean checkDuplicatedUsername(String username) {
        return userService.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public Boolean checkDuplicatedNickname(String nickname) {
        return userService.existsByNickname(nickname);
    }

    public TokenResponse login(Authentication authentication, String deviceType, String clientId) {
        if (!authentication.isAuthenticated()) {
            throw new UserNotFoundException(ResponseStatus.LOGIN_FAILED);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getDetails();
        Long userId = userDetails.getId();

        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_VALID_SECOND);

        String refreshToken = UUID.randomUUID().toString();
        String accessToken = "Bearer " + jwtService.createJwt(authentication, now, expiration);

        UserTokenDto userTokenDto = UserTokenDto.builder()
            .refreshToken(refreshToken)
            .accessToken(accessToken)
            .prevRefreshToken("_")
            .retryCount(0)
            .expirationTime(now.toInstant().plusSeconds(10L).toEpochMilli())
            .build();

        userService.saveRefreshToken(userId, deviceType, clientId, userTokenDto);

        return UserMapper.toTokenResponse(refreshToken, accessToken);
    }

    public void logout(Authentication authentication, String deviceType, String clientId) {
        Long userId = (Long) authentication.getDetails();
        UserTokenDto userTokenDto = userService.getRefreshToken(userId, deviceType, clientId);

        userService.deleteRefreshToken(userId, deviceType, clientId);
        userService.addBlacklist(userTokenDto.getAccessToken(), Duration.ofMinutes(3L));
    }

    public TokenResponse refresh(
        Authentication authentication,
        String accessToken,
        String deviceType,
        String clientId,
        RefreshRequest request
    ) {
        Long userId = (Long) authentication.getDetails();
        String refreshToken = request.getRefreshToken();
        UserTokenDto userTokenDto = userService.getRefreshToken(userId, deviceType, clientId);

        if (!userTokenDto.checkValid(refreshToken)) {
            throw new InvalidRefreshTokenException();
        } else if (userTokenDto.checkRetry(refreshToken)) {
            userTokenDto.retry();
            userService.saveRefreshToken(userId, deviceType, clientId, userTokenDto);

            return UserMapper.toTokenResponse(
                userTokenDto.getRefreshToken(),
                userTokenDto.getAccessToken()
            );
        } else if (userTokenDto.checkReused(refreshToken)) {
            throw new RefreshTokenReuseException();
        } else if (!userTokenDto.equalsAccessToken(accessToken)) {
            throw new InvalidRefreshTokenException();
        }

        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_VALID_SECOND);

        String newRefreshToken = UUID.randomUUID().toString();
        String newAccessToken = "Bearer " + jwtService.refreshJwt(accessToken, now, expiration);

        userTokenDto.refresh(
            newRefreshToken,
            newAccessToken,
            now.toInstant().plusSeconds(10L).toEpochMilli()
        );
        userService.saveRefreshToken(userId, deviceType, clientId, userTokenDto);

        return UserMapper.toTokenResponse(newRefreshToken, newAccessToken);
    }

    @Transactional
    public void registerDevice(String name, DeviceRequest deviceRequest) {
        User user = userService.get(name);
        userService.saveDevice(UserMapper.toUserDevice(user, deviceRequest));
    }

    private Authority createUserAuthority(User user) {
        return Authority.builder()
            .name("ROLE_USER")
            .user(user)
            .build();
    }
}
