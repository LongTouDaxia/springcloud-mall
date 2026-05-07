// OrderPaymentListener.java
package com.longtou.orderservice.mq;

import com.longtou.commoncore.mq.PaymentOrderMessage;
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

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = "order.payment.queue")
public class OrderPaymentListener {
    
    private final OrderService orderService;
    
    @RabbitHandler
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentMessage(PaymentOrderMessage message,
                                     Channel channel
    , @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag  ) throws IOException {

        log.info("收到支付状态更新消息，订单号：{}，支付状态：{}",
                                     message.getOrderNo(), message.getStatus());


        try {
            // 更新订单状态
            boolean success = orderService.updateOrderPayStatus(
                message.getOrderNo(), 
                message.getStatus()
            );
            
            if (success) {
                log.info("订单状态更新成功，订单号：{}", message.getOrderNo());
                //手动确认消息
                channel.basicAck(deliveryTag, false);
                log.info("消息已确认");
            } else {
                log.warn("订单状态更新失败，订单号：{}，可能订单不存在", message.getOrderNo());
                //拒绝消息 不重新入对
                channel.basicNack(deliveryTag, false, false);
            }
            
        } catch (Exception e) {
            log.error("处理支付消息异常，订单号：{}，异常：{}", 
                    message.getOrderNo(), e.getMessage());
            // 拒绝消息，重回队列（可以重试）
            channel.basicNack(deliveryTag, false, true);

        }
    }
}