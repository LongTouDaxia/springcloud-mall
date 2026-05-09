// RabbitMQConfig.java
package com.longtou.orderservice.mq;
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
    // 秒杀下单交换机
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange("cloud_seckill.exchange", true, false);
    }

    // 秒杀下单队列
    @Bean
    public Queue seckillQueue() {
        return new Queue("seckill.order.queue", true);
    }

    // 绑定
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(seckillQueue()).to(orderExchange()).with("seckill.order");
    }


}