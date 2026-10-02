package com.kob.backend.service.user.account;

import java.util.Map;
//编写一个注册板块的接口，只有一个要实现的方法
public interface RegisterService {
    public Map<String, String> register(String username, String password, String confirmedPassword);
} // 注册接口的接口
