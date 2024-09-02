package com.hymin.webtoon_review.user.controller;

import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
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
    public ResponseEntity<RestResponse> login(@Auth Authentication authentication) {
        String jwt = userFacade.login(authentication);

        return ResponseEntity.status(HttpStatus.OK)
            .header("Authorization", jwt)
            .body(RestResponse.onSuccess());
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
