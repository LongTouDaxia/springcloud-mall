// PaymentMessageSender.java
package com.longtou.payservice.mq;


import com.longtou.commoncore.mq.PaymentOrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 消息生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessageSender {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void sendPaymentStatusUpdate(PaymentOrderMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                "payment.exchange",     // 交换机名称
                "order.status.update",  // 路由键
                message                  // 消息内容
            );
            log.info("发送支付状态更新消息成功，订单号：{}，状态：{}", 
                    message.getOrderNo(), message.getStatus());
        } catch (Exception e) {
            log.error("发送消息到MQ失败，订单号：{}，异常：{}", 
                    message.getOrderNo(), e.getMessage());
            // 这里可以记录到数据库，定时任务重发
        }
    }
}