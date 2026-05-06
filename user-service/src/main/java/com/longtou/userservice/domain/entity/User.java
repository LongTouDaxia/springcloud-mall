package com.longtou.userservice.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String password; // 明文，后续可改造
    private String phone;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}