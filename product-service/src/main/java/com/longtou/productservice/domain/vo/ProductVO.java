package com.longtou.productservice.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal normalPrice;
    private LocalDateTime createTime;
}