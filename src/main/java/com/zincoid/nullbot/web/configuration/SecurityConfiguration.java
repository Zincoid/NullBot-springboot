package com.zincoid.nullbot.web.configuration;

import com.zincoid.nullbot.web.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

@Slf4j
@Configuration
public class SecurityConfiguration {

    // // Spring Security 拦截器 (引入完整 Spring Security 依赖时需配置默认放行)
    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //     http.authorizeHttpRequests(auth -> auth
    //                     .requestMatchers("/**").permitAll()
    //                     .anyRequest().permitAll()  // 允许所有请求
    //     )
    //             .csrf(AbstractHttpConfigurer::disable)  // 禁用 CSRF
    //             .formLogin(AbstractHttpConfigurer::disable)  // 禁用表单登录
    //             .httpBasic(AbstractHttpConfigurer::disable)  // 禁用 HTTP Basic
    //             .logout(AbstractHttpConfigurer::disable);  // 禁用默认登出
    //
    //     return http.build();
    // }

    // 密码编码工具
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 秘钥生成工具
    @Bean
    public KeyPair keyPair(JwtProperties properties) throws Exception {
        if (properties.getLocation() == null || !properties.getLocation().exists()) {
            log.warn("▽ [SecurityConfiguration] 未配置网页密钥: 使用随机密钥");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
                properties.getLocation(),
                properties.getPassword().toCharArray()
        );
        return keyStoreKeyFactory.getKeyPair(
                properties.getAlias(),
                properties.getPassword().toCharArray()
        );
    }
}
