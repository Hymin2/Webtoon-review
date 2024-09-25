package com.hymin.gateway.filter;

import com.hymin.gateway.redis.RedisSortedSetService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.stereotype.Component;

@Component
public class IpRateLimiterGatewayFilter extends
    RateLimiterGatewayFilter<IpRateLimiterGatewayFilter.Config> {

    private String[] headerTypes = {"X-Forwarded-For", "Proxy-Client-IP",
        "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

    public IpRateLimiterGatewayFilter(RedisSortedSetService redisSortedSetService) {
        super(Config.class, redisSortedSetService);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String ip = null;

            for (String headerType : headerTypes) {
                ip = exchange.getRequest().getHeaders().getFirst(headerType);
                if (ip != null) {
                    break;
                }
            }

            if (ip == null) {
                ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }

            String request = config.getRequest();
            String immutableIp = ip;

            return isAllowed(ip, request, config.getRequestLimitPerInterval())
                .flatMap(b -> b ? onSuccess(exchange, chain, immutableIp, request,
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
