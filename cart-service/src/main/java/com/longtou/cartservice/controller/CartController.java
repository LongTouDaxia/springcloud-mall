// CartController.java
package com.longtou.cartservice.controller;


import com.longtou.cartservice.service.CartService;
import com.longtou.cartservice.service.CartItemService;
import com.longtou.cartservice.domain.dto.AddToCartDTO;
import com.longtou.cartservice.domain.dto.UpdateCartItemDTO;
import com.longtou.cartservice.domain.vo.CartVO;
import com.longtou.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    /**
     * 从请求头中获取当前登录用户ID（实际微服务中由网关传递）
     */
    private Long getCurrentUserId() {
        // 模拟：从Header获取，生产环境可使用SecurityContextHolder
        // 这里写死或从请求中获取，示例简单返回1L，实际需从Token解析
        // 为演示灵活，改为从请求头读取: @RequestHeader("X-User-Id") Long userId
        // 在方法中通过参数注入，此处仅做演示，实际在接口中加入参数
        return 1L;
    }

    /**
     * 获取当前用户购物车详情
     */
    @GetMapping
    public Result<CartVO> getCart(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long currentUserId = userId != null ? userId : getCurrentUserId();
        CartVO cartVO = cartService.getUserCartWithItems(currentUserId);
        return Result.success(cartVO);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/items")
    public Result<Void> addToCart(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                  @RequestBody @Valid AddToCartDTO addDto) {
        Long currentUserId = userId != null ? userId : getCurrentUserId();
        // 获取或创建购物车
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
    public Result<Void> clearCart(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long currentUserId = userId != null ? userId : getCurrentUserId();
        cartService.clearCart(currentUserId);
        return Result.success();
    }
}