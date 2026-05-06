package com.longtou.productservice.controller;

import com.longtou.common.result.Result;
import com.longtou.productservice.domain.dto.ProductDTO;
import com.longtou.productservice.domain.vo.ProductVO;
import com.longtou.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/add")
    public Result<ProductVO> add(@Valid @RequestBody ProductDTO dto) {
        return Result.success(productService.addProduct(dto));
    }

    @PutMapping("/update")
    public Result<ProductVO> update(@Valid @RequestBody ProductDTO dto) {
        return Result.success(productService.updateProduct(dto));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> get(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success(null);
    }
}