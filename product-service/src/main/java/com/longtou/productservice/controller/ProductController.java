package com.longtou.productservice.controller;

import com.longtou.commoncore.result.Result;
import com.longtou.productservice.domain.dto.ProductDTO;
import com.longtou.productservice.domain.vo.ProductVO;
import com.longtou.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    @GetMapping("/batch")
    public Map<Long, ProductVO> batchGetProducts(@RequestParam("ids") List<Long> ids) {
        log.info("远程调用pproductfeign");
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductVO> productVoList = productService.getProductByIds(ids);
        return productVoList.stream()
                .collect(Collectors.toMap(ProductVO::getId, Function.identity(), (v1, v2) -> v1));
    }

    /**
     * 批量获取商品名称
     * @param ids
     * @return
     */
    @GetMapping("/batchGetName")
    public  Map<Long,String> getProductNameByIds(@RequestParam("ids") List<Long> ids){

        log.info("远程调用pproductfeign");
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductVO> productVoList = productService.getProductByIds(ids);
        return productVoList.stream()
                .collect(Collectors.toMap(
                        ProductVO::getId,
                        ProductVO::getName, (v1, v2) -> v1));//这里表示重复key取第一个
    }

}
