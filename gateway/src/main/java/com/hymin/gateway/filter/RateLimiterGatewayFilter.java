package com.hymin.gateway.filter;

import com.hymin.gateway.redis.RedisSortedSetService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public abstract class RateLimiterGatewayFilter<C> extends AbstractGatewayFilterFactory<C> {

    private final RedisSortedSetService redisSortedSetService;

    public RateLimiterGatewayFilter(Class<C> configClass,
        RedisSortedSetService redisSortedSetService) {
        super(configClass);
        this.redisSortedSetService = redisSortedSetService;
    }

    protected Mono<Void> onSuccess(ServerWebExchange exchange, GatewayFilterChain chain,
        String user, String request, Integer rateLimitIntervalInSeconds) {
        return redisSortedSetService
            .add(user, request, System.nanoTime())
            .then(redisSortedSetService.setExpire(user, request, rateLimitIntervalInSeconds))
            .then(chain.filter(exchange));
    }

    protected Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        return response.setComplete();
    }

    protected boolean isAllowed(String username, String request, Integer requestLimitPerInterval) {
        return Boolean.TRUE.equals(redisSortedSetService.size(username, request)
            .flatMap(size -> {
                if (size != null && size < requestLimitPerInterval) {
                    return Mono.just(true);
                } else {
                    return Mono.just(false);
                }
            }).block());
    }
}
