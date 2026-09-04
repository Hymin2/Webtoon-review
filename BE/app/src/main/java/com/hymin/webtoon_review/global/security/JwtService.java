package com.hymin.webtoon_review.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.key}")
    private String key;

    public String createJwt(Authentication auth, Date now, Date expiration) {
        String authorities = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(", "));

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getDetails();

        return Jwts.builder()
            .setSubject(auth.getName())
            .claim("id", userDetails.getId())
            .claim("username", userDetails.getUsername())
            .claim("nickname", userDetails.getNickname())
            .claim("authorities", authorities)
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .compact();
    }

    public String refreshJwt(String accessToken, Date now, Date expiration) {
        Claims claims;
        try {
            claims = parseJwt(accessToken);
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }

        return Jwts.builder()
            .setSubject(claims.getSubject())
            .claim("id", claims.get("id"))
            .claim("username", claims.get("username"))
            .claim("nickname", claims.get("nickname"))
            .claim("authorities", claims.get("authorities"))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .setIssuedAt(now)
            .setExpiration(expiration)
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
