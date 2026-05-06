package com.longtou.productservice.controller;

import com.longtou.common.result.Result;
import com.longtou.productservice.domain.dto.ProductDTO;
import com.longtou.productservice.domain.entity.Product;
import com.longtou.productservice.domain.vo.ProductVO;
import com.longtou.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @GetMapping("/product/batch")
    public Map<Long, ProductVO> batchGetProducts(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductVO> productVoList = productService.getProductByIds(ids);
        return productVoList.stream()
                .collect(Collectors.toMap(ProductVO::getId, Function.identity(), (v1, v2) -> v1));
    }

}
