// CartServiceImpl.java
package com.longtou.cartservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longtou.cartservice.domain.entity.Cart;
import com.longtou.cartservice.domain.entity.CartItem;
import com.longtou.cartservice.mapper.CartMapper;
import com.longtou.cartservice.service.CartService;
import com.longtou.cartservice.service.CartItemService;
import com.longtou.cartservice.domain.vo.CartItemVO;
import com.longtou.cartservice.domain.vo.CartVO;
import com.longtou.commonapi.client.ProductFeignClient;
import com.longtou.commonapi.domain.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductFeignClient productFeignClient;
    private final CartItemService cartItemService;

    @Override
    public Cart getOrCreateCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        Cart cart = this.getOne(wrapper);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            this.save(cart);
        }
        return cart;
    }

    @Override
    public CartVO getUserCartWithItems(Long userId) {
        Cart cart = getOrCreateCart(userId);

        // 1. 查询该购物车下的所有商品项
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getCartId, cart.getId());
        List<CartItem> items = cartItemService.list(wrapper);

        // 如果购物车为空，直接返回空列表的 CartVO
        if (items.isEmpty()) {
            CartVO cartVO = new CartVO();
            cartVO.setCartId(cart.getId());
            cartVO.setUserId(cart.getUserId());
            cartVO.setCreateTime(cart.getCreateTime());
            cartVO.setItems(Collections.emptyList());
            return cartVO;
        }

        // 2. 提取所有商品ID（去重，可选）
        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        //根据items获取商品信息
        Map<Long, ProductVO> productVOMap = productFeignClient.batchGetProducts(productIds);
        //组装cartitem
        List<CartItemVO> itemVOS = items.stream().map(item -> {
            CartItemVO vo = new CartItemVO();
            vo.setId(item.getId());
            vo.setProductId(item.getProductId());
            vo.setQuantity(item.getQuantity());
            ProductVO productVO = productVOMap.get(item.getProductId());
            vo.setProductVO(productVO);
            return vo;
        }).collect(Collectors.toList());

        // 5. 构建最终 CartVO
        CartVO cartVO = new CartVO();
        cartVO.setCartId(cart.getId());
        cartVO.setUserId(cart.getUserId());
        cartVO.setCreateTime(cart.getCreateTime());
        cartVO.setItems(itemVOS);
        return cartVO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getCartId, cart.getId());
        cartItemService.remove(wrapper);
    }
}