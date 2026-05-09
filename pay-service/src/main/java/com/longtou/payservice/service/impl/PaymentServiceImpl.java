package com.longtou.payservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.longtou.commonapi.client.OrderFeignClient;
import com.longtou.payservice.domain.dto.PaymentCreateDTO;
import com.longtou.payservice.domain.dto.PaymentCallbackDTO;
import com.longtou.payservice.domain.entity.Payment;
import com.longtou.payservice.domain.vo.PaymentVO;
import com.longtou.payservice.mapper.PaymentMapper;

import com.longtou.payservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {
    
    private final PaymentMapper paymentMapper;

    private final OrderFeignClient orderFeignClient;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createPayment(Long userId, PaymentCreateDTO dto) {
        // 检查订单是否已存在支付记录
        Payment existPayment = paymentMapper.selectByOrderNo(dto.getOrderNo());
        Assert.isNull(existPayment, "该订单已存在支付记录");
        
        // 创建支付记录
        Payment payment = new Payment();
        payment.setOrderNo(dto.getOrderNo());
        payment.setUserId(userId);
        payment.setAmount(dto.getAmount());
        payment.setStatus(0); // 未支付

        payment.setChannel(dto.getChannel());//微信支付宝之类的
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        
        save(payment);
        log.info("创建支付记录成功，支付ID：{}，订单号：{}", payment.getId(), dto.getOrderNo());
        
        return convertToVO(payment);
    }

    /**
     * 根据支付结果修改订单状态
     * @param dto
     * @return
     */
    
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public PaymentVO handlePaymentCallback(PaymentCallbackDTO dto) {
        //查询支付记录
        Payment payment = query().eq("id", dto.getPaymentId()).one();
        Assert.notNull(payment, "支付记录不存在");
        
        // 更新支付状态
        payment.setStatus(dto.getStatus());
        payment.setUpdateTime(LocalDateTime.now());

        updateById(payment);
       /*
        //消息队列发送消息
        PaymentOrderMessage message = new PaymentOrderMessage();
        message.setPaymentId(payment.getId());
        message.setOrderNo(payment.getOrderNo());
        message.setStatus(payment.getStatus());
        //支付时间在订单表里这里先不管 订单表再设置
     //   message.setPayTime(payment.getPayTime());
        message.setChannel(payment.getChannel());
        //发送消息
        paymentMessageSender.sendPaymentStatusUpdate(message);
*/
        
        return convertToVO(payment);
    }
    
    @Override
    public PaymentVO getPaymentById(Long id) {
        //查询支付记录
        Payment payment = query().eq("id",id).one();        Assert.notNull(payment, "支付记录不存在");
        return convertToVO(payment);
    }
    
    @Override
    public PaymentVO getPaymentByOrderNo(Long orderNo) {
        Payment payment = paymentMapper.selectByOrderNo(orderNo);
        Assert.notNull(payment, "未找到该订单的支付记录");
        return convertToVO(payment);
    }
    
    @Override
    public List<PaymentVO> getUserPayments(Long userId) {
        List<Payment> payments = paymentMapper.selectByUserId(userId);
        return payments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO mockPay(Long paymentId) {
        Payment payment = getById(paymentId);
        Assert.notNull(payment, "支付记录不存在");
        Assert.isTrue(payment.getStatus() == 0, "只有未支付的订单可以模拟支付");
        
        // 模拟支付处理
        try {
            Thread.sleep(1000); // 模拟支付处理时间
            
            // 模拟支付结果（80%成功，20%失败）
            boolean success = Math.random() > 0.2;
            int status = success ? 1 : 2;
            
            payment.setStatus(status);
            payment.setUpdateTime(LocalDateTime.now());

            updateById(payment);
            
            // 调用订单服务更新订单状态
            // orderClient.updateOrderPayStatus(payment.getOrderNo(), status);
            log.info("模拟支付处理完成，支付ID：{}，结果：{}", 
                    paymentId, success ? "成功" : "失败");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("支付处理被中断", e);
        }
        
        return convertToVO(payment);
    }
    
    /**
     * 实体转VO
     */
    private PaymentVO convertToVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);
        return vo;
    }
}