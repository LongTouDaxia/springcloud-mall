package com.longtou.payservice.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderNo; // 关联订单号
    private Long userId;
    private BigDecimal amount;
    private Integer status; // 0-未支付 1-成功 2-失败
    private String channel; // 默认mock
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}