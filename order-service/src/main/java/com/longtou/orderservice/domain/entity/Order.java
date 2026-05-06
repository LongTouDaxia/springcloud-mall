package com.longtou.orderservice.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`") // 注意order是MySQL关键字，需要反引号转义
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderNo; // 雪花算法生成的订单号
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status; // 1-待支付 2-已支付 3-已取消
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    private LocalDateTime payTime;
}