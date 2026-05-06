// CartItem.java
package com.longtou.cartservice.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("cart_item")
public class CartItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cartId;
    private Long productId;
    private Integer quantity;
}