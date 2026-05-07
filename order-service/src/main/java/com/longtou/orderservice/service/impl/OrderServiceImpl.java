package com.longtou.orderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longtou.commonapi.client.ProductFeignClient;
import com.longtou.commonweb.exception.BusinessException;
import com.longtou.orderservice.domain.dto.OrderCreateDTO;
import com.longtou.orderservice.domain.dto.OrderQueryDTO;
import com.longtou.orderservice.domain.entity.Order;
import com.longtou.orderservice.domain.vo.OrderCreatedVO;
import com.longtou.orderservice.domain.vo.OrderVO;
import com.longtou.orderservice.mapper.OrderMapper;
import com.longtou.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;
    private final AtomicLong idGenerator = new AtomicLong(System.currentTimeMillis());

    private final ProductFeignClient productFeignClient;

    private Long nextOrderNo() {
        // 简化：实际应使用雪花算法或 Redis 自增，这里仅为演示
        return idGenerator.incrementAndGet();
    }

    @Override
    @Transactional
    public OrderCreatedVO createOrder(Long userId, OrderCreateDTO dto) {
        // TODO: 实际应调用 product-service 获取商品单价，这里模拟固定单价 100
        BigDecimal unitPrice = BigDecimal.valueOf(100);
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(dto.getQuantity()));

        Order order = new Order();
        order.setOrderNo(nextOrderNo());
        order.setUserId(userId);
        order.setProductId(dto.getProductId());
        order.setQuantity(dto.getQuantity());
        order.setTotalAmount(totalAmount);
        order.setStatus(1); // 待支付
        order.setCreateTime(LocalDateTime.now());
        save(order);

        OrderCreatedVO orderCreatedVO = new OrderCreatedVO();
        BeanUtils.copyProperties(order, orderCreatedVO);
        return orderCreatedVO;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderNo, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
               .eq(Order::getUserId, userId);
        Order order = getOne(wrapper);
        if (order == null || order.getStatus() != 1) {
            // 只有待支付状态的订单才能取消
            return false;
        }
        order.setStatus(3); // 已取消
        return updateById(order);
    }

    @Override
    public List<OrderVO> listOrders(Long userId, OrderQueryDTO queryDTO) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (queryDTO != null && queryDTO.getStatus() != null) {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(Order::getCreateTime);

        //查询订单信息
        List<Order> orders = list(wrapper);
        if (orders.isEmpty()) {
            return List.of();
        }

        // 2. 收集所有商品ID
        List<Long> productIds = orders.stream()
                .map(Order::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long,String> productList = productFeignClient.getProductNameByIds(productIds);
        return orders.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            //设置订单商品信息  实际中也可以加一些别的字段  返回一个参数
            vo.setProductName(productList.get(order.getProductId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean updateOrderPayStatus(Long orderNo, Integer status) {
        // 1. 查询订单
        Order order = query().eq("order_no", orderNo).one();
        if (order == null) {
            log.error("订单不存在，订单号：{}", orderNo);
            return false;  // 返回false，让消费者确认消息
        }

        // 2. 幂等处理：如果已经是目标状态，直接返回成功
        if (order.getStatus().equals(status)) {
            log.info("订单状态已经是目标状态，无需更新，订单号：{}，状态：{}", orderNo, status);
            return true;  // 返回true，确认消息
        }

        // 3. 只有未支付状态的订单可以更新
        if (order.getStatus() != 0) {
            log.warn("订单当前状态不可更新，订单号：{}，当前状态：{}，目标状态：{}",
                    orderNo, order.getStatus(), status);
            return true;  // 返回true，确认消息（不抛异常）
        }

        // 4. 正常更新

        order.setStatus(status);
        order.setPayTime(LocalDateTime.now());
        updateById(order);

        log.info("更新订单状态成功，订单号：{}，新状态：{}", orderNo, order.getStatus());
        return true;
    }
}