// CartController.java
package com.longtou.cartservice.controller;

import com.longtou.cartservice.domain.dto.AddToCartDTO;
import com.longtou.cartservice.domain.dto.UpdateCartItemDTO;
import com.longtou.cartservice.domain.vo.CartVO;
import com.longtou.cartservice.service.CartItemService;
import com.longtou.cartservice.service.CartService;
import com.longtou.commoncore.utils.UserContext;
import com.longtou.commoncore.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    /**
     * 获取当前用户购物车详情
     */
    @GetMapping
    public Result<CartVO> getCart() {
        Long currentUserId = UserContext.getCurrentUserId();
        CartVO cartVO = cartService.getUserCartWithItems(currentUserId);
        return Result.success(cartVO);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/items")
    public Result<Void> addToCart(@RequestBody @Valid AddToCartDTO addDto) {
        Long currentUserId = UserContext.getCurrentUserId();
        var cart = cartService.getOrCreateCart(currentUserId);
        cartItemService.addToCart(cart.getId(), addDto.getProductId(), addDto.getQuantity());
        return Result.success();
    }

    /**
     * 更新购物车中商品的数量
     */
    @PutMapping("/items")
    public Result<Void> updateCartItem(@RequestBody @Valid UpdateCartItemDTO updateDto) {
        cartItemService.updateQuantity(updateDto.getCartItemId(), updateDto.getQuantity());
        return Result.success();
    }

    /**
     * 删除购物车中的指定商品项
     */
    @DeleteMapping("/items/{cartItemId}")
    public Result<Void> removeCartItem(@PathVariable Long cartItemId) {
        cartItemService.removeCartItem(cartItemId);
        return Result.success();
    }

    /**
     * 清空当前用户购物车
     */
    @DeleteMapping
    public Result<Void> clearCart() {
        Long currentUserId = UserContext.getCurrentUserId();
        cartService.clearCart(currentUserId);
        return Result.success();
    }
}