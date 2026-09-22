package com.kob.backend.service.user.account;

import java.util.Map;
//登录板块的接口
public interface LoginService {
    public Map<String, String> getToken(String username, String password);
}
