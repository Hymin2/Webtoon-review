package com.hymin.webtoon_review.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.key}")
    private String key;
    private final long ACCESS_VALID_SECOND = 1000L * 60 * 60 * 24 * 30;

    public String createJwt(Authentication auth) {
        Date now = new Date();

        String authorities = auth.getAuthorities().stream()
            .map((a) -> a.getAuthority())
            .collect(Collectors.joining(", "));

        return Jwts.builder()
            .setSubject(auth.getName())
            .claim("authorities", authorities)
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + ACCESS_VALID_SECOND))
            .compact();
    }

    public Claims parseJwt(String jwt) throws JwtException {
        if (jwt.contains("Bearer ")) {
            jwt = jwt.replace("Bearer ", "");
        }

        return Jwts.parserBuilder()
            .setSigningKey(getKey())
            .build()
            .parseClaimsJws(jwt)
            .getBody();
    }

    private Key getKey() {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
