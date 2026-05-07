package com.longtou.payservice.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PaymentCreateDTO {
    
    @NotNull(message = "订单号不能为空")
    private Long orderNo;
    
    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;
    
    private String channel = "mock"; // 支付渠道，默认mock
}