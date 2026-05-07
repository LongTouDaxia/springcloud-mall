package com.longtou.payservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.longtou.payservice.domain.dto.PaymentCreateDTO;
import com.longtou.payservice.domain.dto.PaymentCallbackDTO;
import com.longtou.payservice.domain.entity.Payment;
import com.longtou.payservice.domain.vo.PaymentVO;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface PaymentService extends IService<Payment> {
    
    /**
     * 创建支付记录
     */
    PaymentVO createPayment(Long userId, PaymentCreateDTO dto);
    
    /**
     * 处理支付回调
     */
    PaymentVO handlePaymentCallback(PaymentCallbackDTO dto);
    
    /**
     * 根据ID查询支付详情
     */
    PaymentVO getPaymentById(Long id);
    
    /**
     * 根据订单号查询支付记录
     */
    PaymentVO getPaymentByOrderNo(Long orderNo);
    
    /**
     * 查询用户的支付记录
     */
    List<PaymentVO> getUserPayments(Long userId);
    
    /**
     * 模拟支付处理
     */
    PaymentVO mockPay(Long paymentId);
}