package com.w.gulimall.cart.config;

import com.w.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author xin
 * @date 2023-02-10-17:31
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new CartInterceptor())
                .addPathPatterns("/**");
    }
}
