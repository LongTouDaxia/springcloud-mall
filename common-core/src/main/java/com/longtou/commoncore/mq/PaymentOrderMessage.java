// PaymentOrderMessage.java
package com.longtou.commoncore.mq;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息队列调用订单处理
 */
@Data
public class PaymentOrderMessage implements Serializable {
    private Long paymentId;      // 支付记录ID
    private Long orderNo;        // 订单号
    private Integer status;      // 支付状态: 0-未支付 1-成功 2-失败
    private LocalDateTime payTime; // 支付时间
    private String channel;      // 支付渠道
}