package com.hymin.user.controller;

import com.hymin.common.response.BaseResponse;
import com.hymin.common.response.DataResponse;
import com.hymin.user.dto.UserRegister;
import com.hymin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody UserRegister userRegister) {
        userService.register(userRegister);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.onSuccess());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String jwt = userService.login(authentication);

        return ResponseEntity.status(HttpStatus.OK)
            .header("Authorization", jwt)
            .body(BaseResponse.onSuccess());
    }

    @GetMapping("/check/username")
    public ResponseEntity<?> checkDuplicatedUsername(@RequestParam("username") String username) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(DataResponse.onSuccess(userService.existsByUsername(username)));
    }

    @GetMapping("/check/nickname")
    public ResponseEntity<?> checkDuplicatedNickname(@RequestParam("nickname") String nickname) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(DataResponse.onSuccess(userService.existsByNickname(nickname)));
    }
}
