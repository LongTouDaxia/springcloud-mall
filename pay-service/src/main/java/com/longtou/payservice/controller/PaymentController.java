package com.longtou.payservice.controller;

import com.longtou.commoncore.utils.UserContext;
import com.longtou.commoncore.result.Result;
import com.longtou.payservice.domain.dto.PaymentCreateDTO;
import com.longtou.payservice.domain.dto.PaymentCallbackDTO;
import com.longtou.payservice.domain.vo.PaymentVO;
import com.longtou.payservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Validated
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * 创建支付记录
     */
    @PostMapping
    public Result<PaymentVO> createPayment(@RequestBody @Valid PaymentCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        PaymentVO paymentVO = paymentService.createPayment(userId, dto);
        return Result.success(paymentVO);
    }
    
    /**
     * 处理支付回调（外部支付系统回调）
     */
    @PostMapping("/callback")
    public Result<PaymentVO> paymentCallback(@RequestBody @Valid PaymentCallbackDTO dto) {
        PaymentVO paymentVO = paymentService.handlePaymentCallback(dto);
        return Result.success(paymentVO);
    }
    
    /**
     * 模拟支付
     */
    @PostMapping("/mock/{paymentId}")
    public Result<PaymentVO> mockPay(@PathVariable Long paymentId) {
        PaymentVO paymentVO = paymentService.mockPay(paymentId);
        return Result.success(paymentVO);
    }
    
    /**
     * 根据ID查询支付详情
     */
    @GetMapping("/{id}")
    public Result<PaymentVO> getPaymentById(@PathVariable Long id) {
        PaymentVO paymentVO = paymentService.getPaymentById(id);
        return Result.success(paymentVO);
    }
    
    /**
     * 根据订单号查询支付记录
     */
    @GetMapping("/order/{orderNo}")
    public Result<PaymentVO> getPaymentByOrderNo(@PathVariable Long orderNo) {
        PaymentVO paymentVO = paymentService.getPaymentByOrderNo(orderNo);
        return Result.success(paymentVO);
    }
    
    /**
     * 查询当前用户的支付记录
     */
    @GetMapping("/list")
    public Result<List<PaymentVO>> getUserPayments() {
        Long userId = UserContext.getCurrentUserId();
        List<PaymentVO> list = paymentService.getUserPayments(userId);
        return Result.success(list);
    }
    
    /**
     * 支付健康检查
     */
    @GetMapping("/health")
    public Result<String> healthCheck() {
        return Result.success("支付服务运行正常");
    }
}