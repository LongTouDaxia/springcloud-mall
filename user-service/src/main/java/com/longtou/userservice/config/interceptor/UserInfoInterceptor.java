package com.longtou.userservice.config.interceptor;

import com.longtou.common.utils.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取网关传递的用户ID（网关添加的头）
        String userIdStr = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdStr)) {
            try {
                Long userId = Long.parseLong(userIdStr);
                UserContext.setCurrentUserId(userId);
            } catch (NumberFormatException e) {
                // 如果格式不对，忽略，不设置
            }
        }

        // 可选：也获取用户名
        String username = request.getHeader("X-Username");
        if (StringUtils.hasText(username)) {
            UserContext.setCurrentUsername(username);
        }

        return true; // 放行，即使没有用户信息也不拦截（有些接口可能不需要登录）
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}