package com.longtou.gateway.route;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 实现动态路由刷新,监听nacos配置中心
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class NacosRouteDefinitionRepository implements RouteDefinitionRepository {


    private final NacosConfigManager nacosConfigManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;


    @Value("${nacos.route.data-id:gateway-routes.json}")
    private String dataId;
    @Value("${nacos.route.group:ROUTE_GROUP}")
    private String group;

    /**
     * 从nacos中加载路由
     * @return
     */
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        try {
            ConfigService configService = nacosConfigManager.getConfigService();
            String configInfo = configService.getConfig(dataId, group, 5000);
            if (configInfo == null || configInfo.trim().isEmpty()) {
                log.warn("Nacos 中未找到路由配置，dataId={}, group={}", dataId, group);
                return Flux.empty();
            }
            List<RouteDefinition> routeDefinitions = objectMapper.readValue(configInfo,
                    new TypeReference<List<RouteDefinition>>() {});   // 注意花括号位置
            log.info("成功加载 {} 条路由规则", routeDefinitions.size());
            return Flux.fromIterable(routeDefinitions);
        } catch (Exception e) {
            log.error("从 Nacos 加载路由配置失败", e);
            return Flux.empty();
        }
    }
    /**
     * 初始化：监听 Nacos 配置变更，并主动发布 RefreshRoutesEvent
     */
    @PostConstruct
    public void init() throws NacosException {
        log.info("=== NacosRouteDefinitionRepository init ===");   // 强制输出
        ConfigService configService = nacosConfigManager.getConfigService();
        // 启动时打印初始配置（可选）
        String initialConfig = configService.getConfig(dataId, group, 5000);
        log.info("初始路由配置: {}", initialConfig);

        // 添加监听器，当 Nacos 中路由配置变化时，刷新网关路由
        configService.addListener(dataId, group, new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("检测到 Nacos 路由配置变更，开始刷新网关路由...");
                // 发布 RefreshRoutesEvent 会触发网关重新调用 getRouteDefinitions()
                applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
            }

            @Override
            public Executor getExecutor() {
                return null; // 使用默认线程池
            }
        });
    }
    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return null;
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return null;
    }
}
