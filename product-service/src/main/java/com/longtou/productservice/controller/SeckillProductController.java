package com.longtou.productservice.controller;

import com.longtou.commoncore.result.Result;
import com.longtou.productservice.domain.dto.SeckillProductDTO;
import com.longtou.productservice.domain.dto.SeckillStockDTO;
import com.longtou.productservice.domain.vo.SeckillProductVO;
import com.longtou.productservice.service.SeckillProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    @PostMapping("/decrease-stock")
    public Result<Void> decreaseStock(@Valid @RequestBody SeckillStockDTO dto) {
     //   seckillProductService.decreaseStock(dto);
        return Result.success(null);
    }
}