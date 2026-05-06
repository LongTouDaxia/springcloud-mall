package com.longtou.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
配置路径白名单 请求不需要全部拦截
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.whitelist")
public class WhitelistProperties {
    private List<String> paths = new ArrayList<>();
}