package com.hymin.gateway.filter;

import com.hymin.gateway.redis.RedisSortedSetService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;

public class IpRateLimiterGatewayFilter extends
    RateLimiterGatewayFilter<IpRateLimiterGatewayFilter.Config> {

    public IpRateLimiterGatewayFilter(Class<Config> configClass,
        RedisSortedSetService redisSortedSetService) {
        super(configClass, redisSortedSetService);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            String request = config.getRequest();

            if (isAllowed(ip, request, config.getRequestLimitPerInterval())) {
                return onSuccess(exchange, chain, ip, request,
                    config.getRequestLimitPerInterval());
            } else {
                return onError(exchange);
            }
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
