package com.longtou.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举
 * 规则：
 *  - 公共错误：1xxx
 *  - 用户模块：2xxx
 *  - 商品模块：3xxx
 *  - 购物车模块：4xxx
 *  - 订单模块：5xxx
 *  - 支付模块：6xxx
 *  - 网关/鉴权：7xxx
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 公共错误码 (1000-1999) ==========
    SUCCESS(200, "success"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    PARAM_ERROR(1001, "参数错误"),
    PARAM_MISSING(1002, "缺少必要参数"),
    PARAM_TYPE_MISMATCH(1003, "参数类型不匹配"),
    REQUEST_METHOD_NOT_SUPPORTED(1004, "请求方法不支持"),
    RESOURCE_NOT_FOUND(1005, "资源不存在"),
    DATA_INTEGRITY_VIOLATION(1006, "数据完整性违反"),
    TOO_MANY_REQUESTS(1007, "请求过于频繁"),

    // ========== 用户模块 (2000-2999) ==========
    USER_NOT_EXIST(2001, "用户不存在"),
    USERNAME_ALREADY_EXISTS(2002, "用户名已存在"),
    PASSWORD_ERROR(2003, "密码错误"),
    OLD_PASSWORD_INCORRECT(2004, "原密码不正确"),
    USER_DISABLED(2005, "账号已被禁用"),
    UNAUTHORIZED(2006, "未登录或token失效"),
    FORBIDDEN(2007, "无权限访问"),
    TOKEN_EXPIRED(2008, "token已过期"),
    TOKEN_INVALID(2009, "token无效"),
    VERIFICATION_CODE_ERROR(2010, "验证码错误"),
    PHONE_ALREADY_REGISTERED(2011, "手机号已注册"),
    EMAIL_ALREADY_REGISTERED(2012, "邮箱已注册"),
    USER_LOCKED(2013, "账号已锁定，请稍后再试"),
    LOGIN_FAILED_TOO_MANY(2014, "登录失败次数过多，请稍后重试"),

    // ========== 商品模块 (3000-3999) ==========
    PRODUCT_NOT_EXIST(3001, "商品不存在"),
    PRODUCT_OFF_SALE(3002, "商品已下架"),
    PRODUCT_STOCK_INSUFFICIENT(3003, "商品库存不足"),
    PRODUCT_CATEGORY_NOT_EXIST(3004, "商品分类不存在"),
    PRODUCT_ALREADY_OFF_SALE(3005, "商品已是下架状态"),
    PRODUCT_ALREADY_ON_SALE(3006, "商品已是上架状态"),
    PRODUCT_PRICE_ERROR(3007, "商品价格异常"),
    PRODUCT_IMAGE_UPLOAD_FAIL(3008, "商品图片上传失败"),
    PRODUCT_BATCH_UPDATE_ERROR(3009, "商品批量更新失败"),
    PRODUCT_DELETE_FAIL(3010,"商品删除失败"),
    SECKILL_PRODUCT_DELETE_FAIL(3011,"秒杀商品删除失败"),
    SECKILL_PRODUCT_NOT_EXIST(3012,"秒杀商品不存在"),
    // ========== 购物车模块 (4000-4999) ==========
    CART_ITEM_NOT_EXIST(4001, "购物车项不存在"),
    CART_PRODUCT_NOT_FOUND(4002, "购物车中未找到该商品"),
    CART_QUANTITY_EXCEED_LIMIT(4003, "购物车商品数量超过上限"),
    CART_ADD_FAIL(4004, "添加购物车失败"),
    CART_UPDATE_FAIL(4005, "更新购物车失败"),
    CART_DELETE_FAIL(4006, "删除购物车项失败"),
    CART_EMPTY(4007, "购物车为空"),

    // ========== 订单模块 (5000-5999) ==========
    ORDER_NOT_EXIST(5001, "订单不存在"),
    ORDER_STATUS_INVALID(5002, "订单状态异常"),
    ORDER_CANCEL_FAIL(5003, "订单取消失败"),
    ORDER_CONFIRM_FAIL(5004, "订单确认收货失败"),
    ORDER_DELIVERY_FAIL(5005, "订单发货失败"),
    ORDER_CLOSE_FAIL(5006, "订单关闭失败"),
    ORDER_PAY_TIMEOUT(5007, "订单支付超时"),
    ORDER_CREATE_FAIL(5008, "订单创建失败"),
    ORDER_ITEM_NOT_EXIST(5009, "订单项不存在"),
    ORDER_AMOUNT_MISMATCH(5010, "订单金额不一致"),
    ORDER_ALREADY_PAID(5011, "订单已支付，请勿重复操作"),
    ORDER_ALREADY_CANCELLED(5012, "订单已取消"),
    ORDER_ALREADY_CLOSED(5013, "订单已关闭"),
    ORDER_NOT_BELONG_TO_USER(5014, "订单不属于当前用户"),

    // ========== 支付模块 (6000-6999) ==========
    PAYMENT_FAIL(6001, "支付失败"),
    PAYMENT_AMOUNT_MISMATCH(6002, "支付金额与订单金额不符"),
    PAYMENT_CHANNEL_ERROR(6003, "支付渠道错误"),
    PAYMENT_TIMEOUT(6004, "支付超时"),
    PAYMENT_ALREADY_DONE(6005, "支付已完成"),
    PAYMENT_NOT_FOUND(6006, "支付记录不存在"),
    PAYMENT_REFUND_FAIL(6007, "退款失败"),
    PAYMENT_REFUND_AMOUNT_EXCEED(6008, "退款金额超过可退金额"),

    // ========== 网关/鉴权模块 (7000-7999) ==========
    GATEWAY_ROUTE_NOT_FOUND(7001, "网关路由不存在"),
    RATE_LIMIT_EXCEEDED(7002, "访问频率超限"),
    FALLBACK_TRIGGERED(7003, "服务降级触发"),
    REQUEST_TIMEOUT(7004, "请求超时"),
    INVALID_TOKEN(7005, "无效token"),
    MISSING_AUTH_HEADER(7006, "缺少Authorization头"),
    AUTH_TYPE_UNSUPPORTED(7007, "不支持的认证类型"),
    ;

    private final int code;
    private final String msg;

    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) return errorCode;
        }
        return INTERNAL_SERVER_ERROR;
    }
}