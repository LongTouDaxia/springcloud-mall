package com.longtou.orderservice.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.longtou.commonapi.client.OrderFeignClient;
import com.longtou.commonapi.client.ProductFeignClient;
import com.longtou.commonapi.client.SeckillProductFeignClient;
import com.longtou.commonapi.domain.vo.ProductVO;
import com.longtou.commonapi.domain.vo.SeckillProductVO;
import com.longtou.commoncore.mq.SeckillOrderMessage;
import com.longtou.commoncore.result.Result;
import com.longtou.commonweb.exception.BusinessException;
import com.longtou.orderservice.domain.entity.Order;
import com.longtou.orderservice.mapper.OrderMapper;
import com.longtou.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;  // 正确的 Channel
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = "seckill.order.queue")
public class SeckillOrderListener {

    private final SeckillProductFeignClient seckillProductFeignClient;
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final ProductFeignClient productFeignClient;


    @RabbitHandler
    @Transactional(rollbackFor = Exception.class)
    public void HandleSeckillOrderQueue(SeckillOrderMessage message,
                                        Channel channel
    ,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag  ) throws IOException {
        //从消息中获取订单所需信息

        Long seckillId = message.getSeckillId();
        Integer quantity = message.getQuantity();
        Long userId = message.getUserId();
        String orderToken = message.getOrderToken();
        log.info("秒杀消费者收到秒杀id为{}", seckillId);

        try {
            //查询秒杀商品信息
            Result<SeckillProductVO> seckillProductVOResult = seckillProductFeignClient.get(seckillId);
            SeckillProductVO seckillProductVO = seckillProductVOResult.getData();

            if (seckillProductVO == null || seckillProductVO.getStock() < quantity) {
                log.error("库存不足或商品不存在");
                //业务失败直接不重试
                channel.basicAck(deliveryTag, false);
                return;

            }
            //查看是否下单
            List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<>(Order.class)
                    .eq(Order::getUserId, userId)
                    .eq(Order::getProductId, seckillProductVO.getProductId()));
            int count = orders.size();
            //   Long count = orderService.selectOrderByUserId(userId);
            //      Long count = orderFeignClient.selectOrderByUserId(userId);
            if (count == 1) {
                log.info("用户已下单,userid:{}", userId);
                channel.basicAck(deliveryTag, false);
                log.info("消息已确认");
            }

            //扣减库存
            Result<Void> result = seckillProductFeignClient.decreaseStock(userId, Long.valueOf(quantity),seckillId);
            if (result.getCode() != 200) {
                log.error("库存扣减失败");
                channel.basicAck(deliveryTag, false);
                return;
            }


            //创建订单  简化了
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderNo(generateOrderId(userId));
            order.setQuantity(quantity);
            order.setTotalAmount(seckillProductVO.getSeckillPrice().multiply(BigDecimal.valueOf(quantity)));
            order.setStatus(0);  //0-待支付
            order.setProductId(seckillProductVO.getProductId());
            boolean save = orderService.save(order);
            if (!save) {
                throw new BusinessException(500, "订单创建失败");
            }
            //确认消息
            channel.basicAck(deliveryTag, false);
            log.info("订单创建成功，id:{},userid:{}", seckillId, userId);
        }catch (Exception e){
            log.error("消费秒杀消息异常", e);
            // 判断是否需要重试（例如网络异常、DB 暂时不可用等）
            //先不写这么详细了
        }


    }
    //简单的订单id生成器  也可以改成雪花算法
    // 加一个IdWorker 但其实依旧是调用一个函数的事
    private Long generateOrderId(Long userId) {
        // 当前时间戳（毫秒）
        long timestamp = System.currentTimeMillis();

        // 用户ID（取后6位）
        long userPart = userId % 1000000;

        // 线程特定的随机数
        long threadId = Thread.currentThread().getId() % 1000;

        // 使用哈希确保不超范围
        long combined = timestamp ^ (userPart << 20) ^ (threadId << 10);

        // 确保为正数且不超过Long.MAX_VALUE
        return Math.abs(combined % 9000000000000000000L) + 1000000000000000000L;
    }
}
