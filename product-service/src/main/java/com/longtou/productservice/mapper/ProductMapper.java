package com.longtou.productservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longtou.productservice.domain.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}