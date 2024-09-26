package com.hymin.user.service;

import com.hymin.common.utils.PrimaryKeyGenerator;
import com.hymin.user.dto.UserRegister;
import com.hymin.user.entity.Authority;
import com.hymin.user.entity.User;
import com.hymin.user.exception.NicknameAlreadyExistsException;
import com.hymin.user.exception.UserNotFoundException;
import com.hymin.user.exception.UsernameAlreadyExistsException;
import com.hymin.user.mapper.UserMapper;
import com.hymin.user.repository.AuthorityRepository;
import com.hymin.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.key}")
    private String key;

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final PrimaryKeyGenerator primaryKeyGenerator;
    private final long ACCESS_VALID_SECOND = 1000L * 60 * 60 * 24 * 30;

    @Transactional
    public void register(UserRegister userRegister) {
        if (existsByUsername(userRegister.getUsername())) {
            throw new UsernameAlreadyExistsException(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다.");
        } else if (existsByNickname(userRegister.getNickname())) {
            throw new NicknameAlreadyExistsException(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다.");
        }

        userRegister.setPassword(passwordEncoder.encode(userRegister.getPassword()));
        User user = UserMapper.toUser(primaryKeyGenerator, userRegister);
        Authority authority = UserMapper.toUserAuthority(primaryKeyGenerator, user);
        user.setAuthorities(List.of(authority));

        userRepository.save(user);
    }

    public String login(Authentication authentication) {
        if (!authentication.isAuthenticated()) {
            throw new UserNotFoundException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 잘못됐습니다.");
        }

        return "Bearer " + createJwt(authentication);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    private String createJwt(Authentication authentication) {
        Date now = new Date();

        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(", "));

        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim("authorities", authorities)
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + ACCESS_VALID_SECOND))
            .compact();
    }

    private Key getKey() {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
