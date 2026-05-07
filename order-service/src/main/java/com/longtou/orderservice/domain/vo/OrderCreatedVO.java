package com.longtou.orderservice.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderCreatedVO {
    private Long orderNo;          // 订单号
    private BigDecimal totalAmount; // 订单金额
    private Integer status;        // 应为1（待支付）
}