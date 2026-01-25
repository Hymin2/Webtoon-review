package com.hymin.webtoon_review.user.controller;

import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.user.dto.UserRequest.DeviceRequest;
import com.hymin.webtoon_review.user.dto.UserRequest.RefreshRequest;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.facade.UserFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @PostMapping
    public ResponseEntity<RestResponse> register(@Valid @RequestBody RegisterInfo registerInfo) {
        userFacade.register(registerInfo);

        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.onCreated());
    }

    @PostMapping("/login")
    public RestResponse login(
        @Auth Authentication authentication,
        @RequestHeader("X-Device-Type") String deviceType,
        @RequestHeader("X-Client-Id") String clientId) {
        return ApiResponse.onSuccess(userFacade.login(authentication, deviceType, clientId));
    }

    @PostMapping("/logout")
    public RestResponse logout(
        @Auth Authentication authentication,
        @RequestHeader("X-Device-Type") String deviceType,
        @RequestHeader("X-Client-Id") String clientId) {
        userFacade.logout(authentication, deviceType, clientId);

        return RestResponse.onSuccess();
    }

    @PostMapping("/refresh")
    public RestResponse refresh(
        @Auth Authentication authentication,
        @RequestHeader("X-Device-Type") String deviceType,
        @RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("Authorization") String accessToken,
        @RequestBody RefreshRequest request) {
        return ApiResponse.onSuccess(
            userFacade.refresh(authentication, accessToken, deviceType, clientId, request));
    }

    @PostMapping("/push-token")
    public RestResponse registerDevice(
        @Auth Authentication authentication,
        @RequestBody DeviceRequest deviceRequest
    ) {
        userFacade.registerDevice(authentication.getName(), deviceRequest);
        return RestResponse.noContent();
    }

    @GetMapping("/check/username")
    public RestResponse checkDuplicatedUsername(@RequestParam("username") String username) {
        return ApiResponse.onSuccess(userFacade.checkDuplicatedUsername(username));
    }

    @GetMapping("/check/nickname")
    public RestResponse checkDuplicatedNickname(@RequestParam("nickname") String nickname) {
        return ApiResponse.onSuccess(userFacade.checkDuplicatedNickname(nickname));
    }
}
