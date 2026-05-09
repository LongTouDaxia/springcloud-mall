package com.longtou.commonapi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderMessage implements Serializable {
    private Long userId;
    private Long seckillId;
    private Integer quantity;
    private String orderToken;
}