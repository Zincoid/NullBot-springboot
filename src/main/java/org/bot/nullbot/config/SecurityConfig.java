package org.bot.nullbot.config;

import org.bot.nullbot.config.prop.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;

@Configuration
public class SecurityConfig
{
    // Spring Security 拦截
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                        .anyRequest().permitAll()  // 允许所有请求
        )
                .csrf(AbstractHttpConfigurer::disable)  // 禁用 CSRF
                .formLogin(AbstractHttpConfigurer::disable)  // 禁用表单登录
                .httpBasic(AbstractHttpConfigurer::disable)  // 禁用 HTTP Basic
                .logout(AbstractHttpConfigurer::disable);  // 禁用默认登出

        return http.build();
    }

    // 密码编码工具
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 秘钥生成工具
    @Bean
    public KeyPair keyPair(JwtProperties properties){
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
