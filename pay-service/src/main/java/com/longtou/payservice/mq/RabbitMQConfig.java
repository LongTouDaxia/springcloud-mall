// RabbitMQConfig.java
package com.longtou.payservice.mq;
// 订单服务用同样的配置

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // 支付状态更新交换机
    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange("payment.exchange", true, false);
    }
    
    // 订单支付队列
    @Bean
    public Queue orderPaymentQueue() {
        return new Queue("order.payment.queue", true);
    }
    
    // 绑定
    @Bean
    public Binding bindingOrderPayment() {
        return BindingBuilder.bind(orderPaymentQueue())
                .to(paymentExchange())
                .with("order.status.update");
    }
}