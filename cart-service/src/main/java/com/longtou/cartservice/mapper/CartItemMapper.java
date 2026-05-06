// CartItemMapper.java
package com.longtou.cartservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longtou.cartservice.domain.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}