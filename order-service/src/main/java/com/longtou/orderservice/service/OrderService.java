package com.longtou.orderservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.longtou.orderservice.domain.dto.OrderCreateDTO;
import com.longtou.orderservice.domain.dto.OrderQueryDTO;
import com.longtou.orderservice.domain.entity.Order;
import com.longtou.orderservice.domain.vo.OrderCreatedVO;
import com.longtou.orderservice.domain.vo.OrderVO;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface OrderService extends IService<Order> {
    OrderCreatedVO createOrder(Long userId, OrderCreateDTO dto);
    boolean cancelOrder(Long orderNo, Long userId);
    List<OrderVO> listOrders(Long userId, OrderQueryDTO queryDTO);

    boolean updateOrderPayStatus(Long orderNo, Integer status);
}