// Product.java
package com.longtou.productservice.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private BigDecimal normalPrice;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}