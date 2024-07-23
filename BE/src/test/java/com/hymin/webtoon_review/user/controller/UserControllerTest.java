package com.hymin.webtoon_review.user.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.global.security.authentication.UsernamePasswordAuthentication;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.exception.AlreadyUserExistsException;
import com.hymin.webtoon_review.user.exception.UserNotFoundException;
import com.hymin.webtoon_review.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("사용 가능한 username")
    public void availableUsername() throws Exception {
        // given
        String username = "user";

        // when
        when(userService.checkDuplicatedUsername(username))
            .thenReturn(true);

        // then
        mockMvc.perform(get("/users/check/username")
                .queryParam("username", username))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("사용 불가능한 username")
    public void notAvailableUsername() throws Exception {
        // given
        String username = "user";

        // when
        when(userService.checkDuplicatedUsername(username))
            .thenReturn(false);

        // then
        mockMvc.perform(get("/users/check/username")
                .queryParam("username", username))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("사용 가능한 nickname")
    public void availableNickname() throws Exception {
        // given
        String nickname = "nickname";

        // when
        when(userService.checkDuplicatedNickname(nickname))
            .thenReturn(true);

        // then
        mockMvc.perform(get("/users/check/nickname")
                .queryParam("nickname", nickname))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("사용 불가능한 nickname")
    public void notAvailableNickname() throws Exception {
        // given
        String nickname = "nickname";

        // when
        when(userService.checkDuplicatedNickname(nickname))
            .thenReturn(false);

        // then
        mockMvc.perform(get("/users/check/nickname")
                .queryParam("nickname", nickname))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("회원가입 성공")
    public void successRegistration() throws Exception {
        // given
        RegisterInfo registerInfo = RegisterInfo.builder()
            .username("user")
            .nickname("nickname")
            .password("password")
            .build();

        // then
        mockMvc.perform(post("/users")
                .content(new ObjectMapper().writeValueAsString(registerInfo))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    @DisplayName("회원가입 실패")
    public void failedRegistration() throws Exception {
        // given
        RegisterInfo registerInfo = RegisterInfo.builder()
            .username("user")
            .nickname("nickname")
            .password("password")
            .build();

        // when
        doThrow(new AlreadyUserExistsException(ResponseStatus.ALREADY_USER_EXISTS))
            .when(userService).register(registerInfo);

        // then
        mockMvc.perform(post("/users")
                .content(new ObjectMapper().writeValueAsString(registerInfo))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("로그인 성공")
    public void successLogin() throws Exception {
        // given
        Authentication authentication = new UsernamePasswordAuthentication("username",
            "credentials", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // when
        when(userService.login(authentication))
            .thenReturn("Bearer ");

        // then
        mockMvc.perform(post("/users/login"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("로그인 실패")
    public void failedLogin() throws Exception {
        // given
        Authentication authentication = new UsernamePasswordAuthentication("username",
            "credentials");

        // when
        when(userService.login(authentication))
            .thenThrow(UserNotFoundException.class);

        // then
        mockMvc.perform(post("/users/login"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }
}