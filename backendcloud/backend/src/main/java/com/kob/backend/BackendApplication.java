package com.kob.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {
    //项目的入口，jvm执行对应的主函数方法，拉起spring boot/spring mvc/mybits-plus
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
