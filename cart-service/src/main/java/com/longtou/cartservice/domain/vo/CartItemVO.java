// CartItemVO.java
package com.longtou.cartservice.domain.vo;

import com.longtou.mallapi.domain.vo.ProductVO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemVO {
    private Long id;
    private Long productId;    // 商品ID
    private Integer quantity;  // 数量
    private ProductVO productVO;

}