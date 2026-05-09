package com.longtou.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {


    /**
     * 按IP限流的解析器
     * 注意：Bean名称必须和Nacos配置中的 #{@ipKeyResolver} 对应
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            // 获取客户端IP地址
            String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(ip);
        };
    }

}
