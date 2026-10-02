package com.kob.matchingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// 这个配置文件的作用是让Spring容器生成一个RestTemplate的Bean变量
// 当我们需要在其他文件中用到一些变量时，直接如该文件一样的格式把它注册为Bean变量
@Configuration // 声明这个是一个配置类，Spring容器启动时会扫描到它，把它当作配置图纸
public class RestTemplateConfig {
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
