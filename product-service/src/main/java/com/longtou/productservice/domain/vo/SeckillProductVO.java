package com.longtou.productservice.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillProductVO {
    private Long id;
    private Long productId;
    private String productName;       // 关联的普通商品名称
    private String productDesc;       // 商品描述
    private BigDecimal normalPrice;   // 普通价格
    private BigDecimal seckillPrice;
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer version;
}