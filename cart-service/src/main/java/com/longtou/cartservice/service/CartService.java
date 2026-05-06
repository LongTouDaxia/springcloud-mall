// CartService.java
package com.longtou.cartservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.longtou.cartservice.domain.entity.Cart;
import com.longtou.cartservice.domain.vo.CartVO;
import org.springframework.stereotype.Service;

@Service
public interface CartService extends IService<Cart> {
    /**
     * 获取或创建用户的购物车（每个用户只有一个购物车）
     * @param userId 用户ID
     * @return Cart
     */
    Cart getOrCreateCart(Long userId);

    /**
     * 获取用户购物车详情（包含商品项）
     * @param userId 用户ID
     * @return CartVO
     */
    CartVO getUserCartWithItems(Long userId);

    /**
     * 清空用户购物车（删除所有商品项，保留购物车记录）
     * @param userId 用户ID
     */
    void clearCart(Long userId);
}