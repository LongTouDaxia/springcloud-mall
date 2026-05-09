package com.longtou.commoncore.mq;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀信息
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SeckillOrderMessage implements Serializable {
    private Long userId;
    private Long seckillId;
    private Integer quantity;

    private String orderToken;  //返回前端唯一订单id
}
