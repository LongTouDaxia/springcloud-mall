// UpdateCartItemDTO.java
package com.longtou.cartservice.domain.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UpdateCartItemDTO {
    @NotNull(message = "购物车项ID不能为空")
    private Long cartItemId;

    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量必须大于等于0，0表示删除")
    private Integer quantity;
}