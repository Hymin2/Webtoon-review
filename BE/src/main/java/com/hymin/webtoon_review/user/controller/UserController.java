package com.hymin.webtoon_review.user.controller;

import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.user.dto.UserRequest.LoginInfo;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public RestResponse register(@RequestBody RegisterInfo registerInfo) {
        userService.register(registerInfo);

        return RestResponse.onCreated();
    }

    @PostMapping("/login")
    public RestResponse login(@RequestBody LoginInfo loginInfo) {
        return ApiResponse.onSuccess(userService.login(loginInfo));
    }
}
