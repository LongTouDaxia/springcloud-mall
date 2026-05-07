package com.longtou.payservice.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVO {
    private Long id;
    private Long orderNo;
    private Long userId;
    private BigDecimal amount;
    private Integer status; // 0-未支付 1-成功 2-失败
    private String channel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public String getStatusDesc() {
        switch (status) {
            case 0: return "未支付";
            case 1: return "支付成功";
            case 2: return "支付失败";
            default: return "未知状态";
        }
    }
}