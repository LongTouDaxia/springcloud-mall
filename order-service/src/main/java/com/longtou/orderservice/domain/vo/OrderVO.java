package com.longtou.orderservice.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private Long orderNo;
    private Long productId;
    private String productName;   // 新增：商品名称
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}