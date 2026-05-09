package com.longtou.orderservice.service.mq;

import com.longtou.commonapi.client.SeckillProductFeignClient;
import com.longtou.commonapi.domain.vo.SeckillProductVO;
import com.longtou.commoncore.mq.SeckillOrderMessage;
import com.longtou.commoncore.result.Result;
import com.longtou.orderservice.domain.entity.Order;
import com.longtou.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RocketMQMessageListener(
        topic = "seckill-topic",
        consumerGroup = "order-consumer-group"  // 消费者组名，可以自定义
)
@Component
@Slf4j
@RequiredArgsConstructor
public class SeckillOrderConsumer implements RocketMQListener<SeckillOrderMessage> {

    private final SeckillProductFeignClient  seckillProductFeignClient;
    private final OrderService orderService;

    @Override
    public void onMessage(SeckillOrderMessage message) {

        Long userId = message.getUserId();
        Long seckillId = message.getSeckillId();
        Integer quantity = message.getQuantity();
        String orderToken = message.getOrderToken();

        log.info("收到订单创建消息: userId={}, seckillId={}, quantity={}", userId, seckillId, quantity);

        try {
            //查询商品信息 用户是否下单
            Result<SeckillProductVO> result = seckillProductFeignClient.get(seckillId);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                log.error("调用商品服务失败，seckillId={}", seckillId);
                throw new RuntimeException("查询秒杀商品失败，需要重试");
            }
            SeckillProductVO seckillProductVO = result.getData();

            //不用做库存校验 因为消息发送者本地事务已经执行了

            //检验是否重复下单
            Long count = orderService.query().eq("user_id", userId)
                    .eq("product_id", seckillProductVO.getProductId()).count();
            if (count > 0) {
                log.warn("用户已下单，忽略重复消息: userId={}, seckillId={}", userId,seckillId);
                return;  // 业务重复，不重试
            }
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderNo(generateOrderId(userId));
            order.setQuantity(quantity);
            // 价格示例，实际从商品服务获取
            order.setTotalAmount(seckillProductVO.getSeckillPrice().multiply(BigDecimal.valueOf(quantity)));            order.setStatus(0); // 待支付
            order.setProductId(1L); // 实际从秒杀商品获取 productId
            boolean saved = orderService.save(order);
            if (!saved) {
                log.error("订单创建失败，数据库插入异常");
                throw new RuntimeException("订单创建失败，需要重试");            }

            log.info("订单创建成功: orderId={}, orderToken={}", order.getId(), orderToken);
        }catch (Exception e){
            log.error("创建订单失败，消息将重试: {}", e.getMessage(), e);
            // 抛出异常会触发 RocketMQ 的重试机制（默认重试16次）
            throw new RuntimeException(e);
        }

    }

    private Long generateOrderId(Long userId) {
        long timestamp = System.currentTimeMillis();
        long userPart = userId % 1000000;
        long threadId = Thread.currentThread().getId() % 1000;
        long combined = timestamp ^ (userPart << 20) ^ (threadId << 10);
        return Math.abs(combined % 9000000000000000000L) + 1000000000000000000L;
    }
}
