package com.longtou.commonapi.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


//实现复用DTO
@Data
public class ProductDTO {
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "普通价格不能为空")
    private BigDecimal normalPrice;
}