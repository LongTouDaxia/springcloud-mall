package com.longtou.orderservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longtou.orderservice.domain.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}