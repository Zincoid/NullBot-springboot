package com.zincoid.nullbot.web.configuration;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.web.interceptor.WebInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

    private final WebInterceptor webInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {  // 注册拦截器
        registry.addInterceptor(webInterceptor)
                .addPathPatterns("/**");
        // .excludePathPatterns("/xx");
        //  /* 一级路径 /** 任意级路径
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有源 但支持凭证
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
