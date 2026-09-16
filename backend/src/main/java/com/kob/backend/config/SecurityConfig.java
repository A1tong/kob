package com.kob.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration //告诉Spring，这是一个配置类启动时请加载它，并处理里面的@Bean方法
@EnableWebSecurity //告诉Spring Boot，启用Spring Security的Web安全支持
public class SecurityConfig {

    @Bean //实例这个方法的返回值，作为一个Bean交给Spring容器管理
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    } //返回类型是PasswordEncoder接口，实际返回的是BCryptPasswordEncoder实例
}