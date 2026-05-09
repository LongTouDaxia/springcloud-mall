package com.longtou.commonapi.client;

import com.longtou.commonapi.domain.vo.SeckillProductVO;
import com.longtou.commoncore.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@FeignClient(name = "product-service",contextId = "seckillProductFeign")
public interface SeckillProductFeignClient {

    @GetMapping("/seckill/{id}")
    Result<SeckillProductVO> get(@PathVariable("id") Long id);


    @PostMapping("/seckill/decrease")
    Result<Void> decreaseStock(@RequestParam("userId") Long userId,
                               @RequestParam("quantity") Long quantity,
                               @RequestParam("seckillId") Long seckillId);
}
