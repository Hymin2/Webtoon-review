package com.hymin.gateway.filter;

import com.hymin.gateway.redis.RedisSortedSetService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.stereotype.Component;

@Component
public class UserRateLimiterGatewayFilter extends
    RateLimiterGatewayFilter<UserRateLimiterGatewayFilter.Config> {

    public UserRateLimiterGatewayFilter(RedisSortedSetService redisSortedSetService) {
        super(Config.class, redisSortedSetService);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String username = exchange.getRequest().getHeaders().getFirst("User-Id");
            String request = config.getRequest();

            return isAllowed(username, request, config.getRequestLimitPerInterval())
                .flatMap(b -> b ? onSuccess(exchange, chain, username, request,
                    config.getRequestLimitPerInterval()) : onError(exchange));
        };
    }

    @Getter
    @Setter
    public static class Config {

        private String request;
        private Integer rateLimitIntervalInSeconds;
        private Integer requestLimitPerInterval;
    }
}
