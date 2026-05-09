package com.longtou.orderservice.controller;

import com.longtou.commoncore.utils.UserContext;
import com.longtou.commoncore.result.Result;
import com.longtou.orderservice.domain.dto.OrderCreateDTO;
import com.longtou.orderservice.domain.dto.OrderQueryDTO;
import com.longtou.orderservice.domain.vo.OrderCreatedVO;
import com.longtou.orderservice.domain.vo.OrderVO;
import com.longtou.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public Result<OrderCreatedVO> createOrder(@RequestBody @Valid OrderCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        OrderCreatedVO orderVO = orderService.createOrder(userId, dto);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{orderNo}")
    public Result<Void> cancelOrder(@PathVariable Long orderNo) {
        Long userId = UserContext.getCurrentUserId();
        boolean success = orderService.cancelOrder(orderNo, userId);
        return success ? Result.success() : Result.fail("订单取消失败，可能订单不存在或状态不正确");
    }

    /**
     * 查询当前用户的订单列表（可选按状态筛选）
     */
    @GetMapping("/list")
    public Result<List<OrderVO>> listOrders(@Valid OrderQueryDTO queryDTO) {
        Long userId = UserContext.getCurrentUserId();
        List<OrderVO> list = orderService.listOrders(userId, queryDTO);
        return Result.success(list);
    }


    @GetMapping("/user")
    public Long selectOrderByUserId(@RequestParam Long userId){

        Long count = orderService.selectOrderByUserId(userId);
        return count;



    }
}