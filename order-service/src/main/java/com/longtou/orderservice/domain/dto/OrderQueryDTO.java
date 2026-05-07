package com.longtou.orderservice.domain.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {
    private Integer status; // 1-待支付 2-已支付 3-已取消，可选
}