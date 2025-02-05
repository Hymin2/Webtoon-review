package com.hymin.webtoon_review.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.global.security.JwtService;
import com.hymin.webtoon_review.user.dto.UserRequest.RegisterInfo;
import com.hymin.webtoon_review.user.exception.AlreadyUserExistsException;
import com.hymin.webtoon_review.user.exception.UserNotFoundException;
import com.hymin.webtoon_review.user.facade.UserFacade;
import com.hymin.webtoon_review.user.repository.AuthorityRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private AuthorityRepository authorityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserFacade userFacade;

    @Test
    @DisplayName("사용 가능한 username")
    public void availableUsername() {
        // given
        String username = "user";

        // when
        when(userService.existsByUsername(username)).thenReturn(false);

        // then
        Boolean b = userFacade.checkDuplicatedUsername(username);

        assertEquals(b, true);
    }

    @Test
    @DisplayName("사용 불가능한 username")
    public void notAvailableUsername() {
        // given
        String username = "user";

        // when
        when(userService.existsByUsername(username)).thenReturn(true);

        // then
        Boolean b = userFacade.checkDuplicatedUsername(username);

        assertEquals(b, false);
    }

    @Test
    @DisplayName("사용 가능한 nickname")
    public void availableNickname() {
        // given
        String nickname = "nickname";

        // when
        when(userService.existsByNickname(nickname)).thenReturn(false);

        // then
        Boolean b = userFacade.checkDuplicatedNickname(nickname);

        assertEquals(b, false);
    }

    @Test
    @DisplayName("사용 불가능한 nickname")
    public void notAvailableNickname() {
        // given
        String nickname = "nickname";

        // when
        when(userService.existsByNickname(nickname)).thenReturn(true);

        // then
        Boolean b = userFacade.checkDuplicatedNickname(nickname);

        assertEquals(b, true);
    }

    @Test
    @DisplayName("회원가입 성공")
    public void successRegistration() {
        // given
        RegisterInfo registerInfo = RegisterInfo.builder()
            .username("user")
            .nickname("nickname")
            .password("password")
            .build();

        // when
        when(userService.existsByUsernameOrNickname(registerInfo.getUsername(),
            registerInfo.getNickname()))
            .thenReturn(false);

        // then
        userFacade.register(registerInfo);
    }

    @Test
    @DisplayName("회원가입 실패")
    public void failedRegistration() {
        // given
        RegisterInfo registerInfo = RegisterInfo.builder()
            .username("user")
            .nickname("nickname")
            .password("password")
            .build();

        // when
        when(userService.existsByUsernameOrNickname(registerInfo.getUsername(),
            registerInfo.getNickname()))
            .thenReturn(true);

        // then
        assertThrows(AlreadyUserExistsException.class, () -> {
            userFacade.register(registerInfo);
        });
    }

    @Test
    @DisplayName("로그인 성공")
    public void successLogin() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "password",
            List.of(new SimpleGrantedAuthority("USER")));

        // then
        String jwt = userFacade.login(authentication);

        assertEquals(true, jwt.contains("Bearer "));
    }

    @Test
    @DisplayName("로그인 실패")
    public void failedLogin() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "password");

        // then
        assertThrows(UserNotFoundException.class, () -> {
            userFacade.login(authentication);
        });
    }
}