// CartItemServiceImpl.java
package com.longtou.cartservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longtou.cartservice.domain.entity.CartItem;
import com.longtou.cartservice.mapper.CartItemMapper;
import com.longtou.cartservice.service.CartItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long cartId, Long productId, Integer quantity) {
        // 查找是否已存在该商品
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getCartId, cartId)
               .eq(CartItem::getProductId, productId);
        CartItem existing = this.getOne(wrapper);

        if (existing != null) {
            // 存在则增加数量
            existing.setQuantity(existing.getQuantity() + quantity);
            this.updateById(existing);
        } else {
            // 不存在则新增
            CartItem newItem = new CartItem();
            newItem.setCartId(cartId);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            this.save(newItem);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            // 数量为0则删除该商品项
            this.removeById(cartItemId);
        } else {
            CartItem item = this.getById(cartItemId);
            if (item != null) {
                item.setQuantity(quantity);
                this.updateById(item);
            } else {
                throw new RuntimeException("购物车项不存在");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCartItem(Long cartItemId) {
        this.removeById(cartItemId);
    }
}