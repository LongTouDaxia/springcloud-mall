// CartMapper.java
package com.longtou.cartservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longtou.cartservice.domain.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}