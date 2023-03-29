package com.w.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author xin
 * @date 2022-12-30-14:28
 */
@Configuration
public class GulimallCorsConfiguration {
    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //1.配置跨域
        corsConfiguration.addAllowedHeader("*");    //允许那些头，进行跨域
        corsConfiguration.addAllowedMethod("*");    //允许那些请求方式进行跨域
        corsConfiguration.addAllowedOrigin("*");    //允许那些请求来源进行跨域
        corsConfiguration.setAllowCredentials(true);    //是否携带cookic进行跨域
        source.registerCorsConfiguration("/**",corsConfiguration);  //允许所有请求进行跨域
        return new CorsWebFilter(source);
    }
}
