// CartItemVO.java
package com.longtou.cartservice.domain.vo;

import com.longtou.commonapi.domain.vo.ProductVO;
import lombok.Data;

@Data
public class CartItemVO {
    private Long id;
    private Long productId;    // 商品ID
    private Integer quantity;  // 数量
    private ProductVO productVO;

}