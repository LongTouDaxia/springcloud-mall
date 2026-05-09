/*package com.longtou.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
  想用jemter测量秒杀接口 可以使用这个过滤器  不需要传入jwt令牌
 *
// TestAuthFilter.java
@Component
@Slf4j
public class TestAuthFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        log.info("请求通过网关");


        // 如果是测试请求，直接生成用户ID
        String testHeader = exchange.getRequest().getHeaders().getFirst("X-Test-User");
        
        if (testHeader != null) {
            try {
                Long userId = Long.parseLong(testHeader);
                
                // 将用户ID放入请求头
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .build();
                
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (NumberFormatException e) {
                // 格式错误，继续正常流程
            }
        }
        
        return chain.filter(exchange);
    }
}
*/
