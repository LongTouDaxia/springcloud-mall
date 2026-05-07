package com.longtou.payservice.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class PaymentCallbackDTO {
    
    @NotNull(message = "支付记录ID不能为空")
    private Long paymentId;
    
    @NotNull(message = "支付状态不能为空")
    private Integer status; // 0-支付中 1-支付成功 2-支付失败
}