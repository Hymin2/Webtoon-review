package com.hymin.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthorizationGatewayFilter extends AbstractGatewayFilterFactory<AuthorizationGatewayFilter.Config> {

    @Value("${jwt.key}")
    private String key;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!config.isUse()) {
                return chain.filter(exchange);
            } else if (!existsAuthorization(exchange)) {
                return onError(exchange);
            }

            String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
            String username = parseAuthorization(authorization);

            if (username == null) {
                return onError(exchange);
            }

            exchange.getRequest()
                .mutate()
                .header("User-Id", username)
                .build();

            return chain.filter(exchange);
        };
    }

    private String parseAuthorization(String authorization) {
        if (authorization == null) {
            return null;
        }

        try {
            String jwt = authorization.replace("Bearer ", "");

            return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private Key getKey() {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean existsAuthorization(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        return request.getHeaders().containsKey("Authorization");
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        return response.setComplete();
    }

    @Getter
    @Setter
    public static class Config {
        private boolean use;
    }
}
