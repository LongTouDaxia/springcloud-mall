package com.longtou.commonapi.client;



import com.longtou.commonapi.domain.vo.ProductVO;
import com.longtou.commoncore.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service",contextId = "productFeign")
public interface ProductFeignClient {
    /**
     * 批量根据商品ID获取商品信息
     * @param ids 商品ID列表，例如 "1001,1002,1003"
     * @return 商品ID 到 ProductVO 的映射
     */
    @GetMapping("/product/batch")
    Map<Long, ProductVO> batchGetProducts(@RequestParam("ids") List<Long> ids);

    @GetMapping("/product/batchGetName")
    Map<Long,String> getProductNameByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/{id}")
    Result<ProductVO> get(@PathVariable("id") Long id) ;


}
