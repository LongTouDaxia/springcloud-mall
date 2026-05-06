// CartItemService.java
package com.longtou.cartservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.longtou.cartservice.domain.entity.CartItem;
import org.springframework.stereotype.Service;

@Service
public interface CartItemService extends IService<CartItem> {
    /**
     * 添加商品到购物车（如已存在则增加数量）
     * @param cartId    购物车ID
     * @param productId 商品ID
     * @param quantity  增加的数量
     */
    void addToCart(Long cartId, Long productId, Integer quantity);

    /**
     * 更新购物车中某个商品的数量
     * @param cartItemId 购物车项ID
     * @param quantity   新数量（若为0则删除该项）
     */
    void updateQuantity(Long cartItemId, Integer quantity);

    /**
     * 删除购物车中的某个商品项
     * @param cartItemId 购物车项ID
     */
    void removeCartItem(Long cartItemId);
}