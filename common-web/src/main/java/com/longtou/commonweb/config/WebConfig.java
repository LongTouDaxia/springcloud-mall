package com.longtou.commonweb.config;

import com.longtou.commonweb.config.interceptor.UserInfoInterceptor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


  //  private final UserInfoInterceptor userInfoInterceptor;


    // 不再需要注入，改为手动创建 Bean
    @Bean
    public UserInfoInterceptor userInfoInterceptor() {
        return new UserInfoInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInfoInterceptor())
                .addPathPatterns("/**");   // 拦截所有请求
                // 如果你想让某些接口不需要用户信息（比如登录、注册），可以排
                // .excludePathPatterns("/user/login", "/user/register");
    }
}