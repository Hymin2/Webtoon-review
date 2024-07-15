package com.hymin.webtoon_review.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequest {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterInfo {

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9\\-_]{4,20}$")
        private String username;

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9~․!@#$%^&*()_\\-+=\\[\\]\\|\\\\\\;:‘“<>,.\\/?]{4,20}$")
        private String password;

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9가-힣\\-_]{4,20}$")
        private String nickname;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginInfo {

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9\\-_]{4,20}$")
        private String username;

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9~․!@#$%^&*()_\\-+=\\[\\]\\|\\\\\\;:‘“<>,.\\/?]{4,20}$")
        private String password;
    }
}
