package com.longtou.commonapi.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "order-service")
public interface OrderFeignClient {
}
