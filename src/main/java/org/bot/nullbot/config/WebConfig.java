package org.bot.nullbot.config;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer
{
    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {  // 注册拦截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**");
        // .excludePathPatterns("/NullBot");
        //  /* 一级路径 /** 任意级路径
    }
}
