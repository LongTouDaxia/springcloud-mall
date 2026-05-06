// CartVO.java
package com.longtou.cartservice.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartVO {
    private Long cartId;
    private Long userId;
    private LocalDateTime createTime;
    private List<CartItemVO> items;   // 购物车中的商品项列表
}