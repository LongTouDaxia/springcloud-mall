package com.longtou.mallapi.client;

import com.longtou.mallapi.domain.dto.ProductDTO;
import com.longtou.mallapi.domain.vo.ProductVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service")
public interface ProductFeignClient {
    /**
     * 批量根据商品ID获取商品信息
     * @param ids 商品ID列表，例如 "1001,1002,1003"
     * @return 商品ID 到 ProductVO 的映射
     */
    @GetMapping("/product/batch")
    Map<Long, ProductVO> batchGetProducts(@RequestParam("ids") List<Long> ids);
}
