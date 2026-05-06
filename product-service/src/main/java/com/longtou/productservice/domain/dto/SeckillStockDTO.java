package com.longtou.productservice.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class SeckillStockDTO {
    @NotNull(message = "秒杀商品ID不能为空")
    private Long seckillId;

    @Positive(message = "扣减数量必须为正数")
    private Integer quantity;
}