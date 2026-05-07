package com.longtou.payservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longtou.payservice.domain.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
    
    /**
     * 根据订单号查询支付记录
     */
    @Select("SELECT * FROM payment WHERE order_no = #{orderNo} ORDER BY id DESC LIMIT 1")
    Payment selectByOrderNo(@Param("orderNo") Long orderNo);
    
    /**
     * 更新支付状态
     */
    @Update("UPDATE payment SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 根据用户ID查询支付记录
     */
    @Select("SELECT * FROM payment WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Payment> selectByUserId(@Param("userId") Long userId);
}