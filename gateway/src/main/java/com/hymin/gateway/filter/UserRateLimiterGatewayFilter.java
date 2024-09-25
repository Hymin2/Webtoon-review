package com.hymin.gateway.filter;

import com.hymin.gateway.redis.RedisSortedSetService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserRateLimiterGatewayFilter extends
    AbstractGatewayFilterFactory<UserRateLimiterGatewayFilter.Config> {

    private final RedisSortedSetService redisSortedSetService;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String username = exchange.getRequest().getHeaders().getFirst("User-Id");
            String request = config.getRequest();

            if (isAllowed(username, request, config.getRequestLimitPerInterval())) {

                return redisSortedSetService
                    .add(username, request, System.nanoTime())
                    .then(redisSortedSetService.setExpire(username, request,
                        config.getRateLimitIntervalInSeconds()))
                    .then(chain.filter(exchange));
            } else {
                return onError(exchange);
            }
        };
    }

    private boolean isAllowed(String username, String request, Integer requestLimitPerInterval) {
        return Boolean.TRUE.equals(redisSortedSetService.size(username, request)
            .flatMap(size -> {
                if (size != null && size < requestLimitPerInterval) {
                    return Mono.just(true);
                } else {
                    return Mono.just(false);
                }
            }).block());
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        return response.setComplete();
    }

    @Getter
    @Setter
    public static class Config {

        private String request;
        private Integer rateLimitIntervalInSeconds;
        private Integer requestLimitPerInterval;
    }
}
