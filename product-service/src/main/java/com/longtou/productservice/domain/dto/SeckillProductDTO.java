package com.longtou.productservice.domain.dto;

import lombok.Data;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillProductDTO {
    private Long id;

    @NotNull(message = "关联的商品ID不能为空")
    private Long productId;

    @NotNull(message = "秒杀价格不能为空")
    private BigDecimal seckillPrice;

    @Positive(message = "库存必须大于0")
    private Integer stock;

    @NotNull(message = "秒杀开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime startTime;

    @NotNull(message = "秒杀结束时间不能为空")
    @Future(message = "结束时间必须是未来时间")
    private LocalDateTime endTime;
}