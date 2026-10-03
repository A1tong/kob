package com.kob.botrunningsystem.controller;

import com.kob.botrunningsystem.service.BotRunningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

// 凡是controller里面的url访问，都是tomcat开一条线程进行代码的运行
@RestController
public class BotRunningController {
    // 当Spring容器读到@Autowired注解后，会扫描标注代码
    // 发现应该是一个接口的实例对象，于是会去从容器中找实现该接口的唯一Bean对象并注入
    @Autowired
    private BotRunningService botRunningService;

    @PostMapping("/bot/add/")
    public String addBot(@RequestParam MultiValueMap<String, String> data) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(data.getFirst("user_id")));
        String botCode = data.getFirst("bot_code");
        String input = data.getFirst("input");
        return botRunningService.addBot(userId, botCode, input);
    }
}
