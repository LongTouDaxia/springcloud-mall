package com.longtou.productservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.longtou.commoncore.result.Result;
import com.longtou.productservice.domain.dto.SeckillProductDTO;
import com.longtou.productservice.domain.dto.SeckillStockDTO;
import com.longtou.productservice.domain.vo.SeckillProductVO;
import com.longtou.productservice.service.SeckillProductService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillProductController {

    private final SeckillProductService seckillProductService;

    @PostMapping("/add")
    public Result<SeckillProductVO> add(@Valid @RequestBody SeckillProductDTO dto) {
        return Result.success(seckillProductService.addSeckillProduct(dto));
    }

    @PutMapping("/update")
    public Result<SeckillProductVO> update(@Valid @RequestBody SeckillProductDTO dto) {
        return Result.success(seckillProductService.updateSeckillProduct(dto));
    }

    @GetMapping("/{id}")
    public Result<SeckillProductVO> get(@PathVariable Long id) {
        return Result.success(seckillProductService.getSeckillProductById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        seckillProductService.deleteSeckillProduct(id);
        return Result.success(null);
    }

    @GetMapping("/ongoing")
    public Result<List<SeckillProductVO>> ongoing() {
        return Result.success(seckillProductService.listOngoingSeckillProducts());
    }

    @PostMapping("/decrease")
    Result<Void> decreaseStock(@RequestParam("userId") Long userId,
                               @RequestParam("quantity") Long quantity,
                               @RequestParam("seckillId") Long seckillId) {
        seckillProductService.decreaseStock(userId,quantity,seckillId);
        return Result.success(null);
    }


    @PostMapping("/doSeckill")
    public Result<Map<String,String>> doSeckill(@Valid @RequestBody SeckillStockDTO seckillStockDTO){


        Map<String,String> orderData = seckillProductService.doSeckill(seckillStockDTO);
        return Result.success(orderData);
    }

    /**
     * 测试网管限流
     * @return
     */
    @SentinelResource(value = "seckillTest",blockHandler = "",fallback = "")
    @GetMapping("/test")
    public String test() {
        return "秒杀测试接口 - 时间：" + new Date();
    }


}