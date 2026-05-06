package com.longtou.productservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longtou.productservice.domain.entity.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {

    @Update("UPDATE seckill_product SET stock = stock - #{quantity}, version = version + 1 " +
            "WHERE id = #{id} AND stock >= #{quantity} AND version = #{version}")
    int decreaseStockWithVersion(@Param("id") Long id,
                                 @Param("quantity") Integer quantity,
                                 @Param("version") Integer version);
}