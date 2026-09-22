package com.kob.backend.service.impl.user.account;

import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.account.InfoService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// 把这个类注册为Spring Bean，容器启动时@ComponentScan扫到它，登记至BeanDefinition，实例化后放入单例池
@Service
public class InfoServiceImpl implements InfoService { // 实现InfoService接口，接口里定义了getinfo()方法
    @Override // 重写getinfo方法
    public Map<String, String> getinfo() {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    //Spring Security通过过滤器验证发来的token，知道是哪个用户，并将这个用户信息存放在它这里
    //然后通过SecurityContextHolder.getContext().getAuthentication()拿取Authentication对象

        // 取出用户信息主体，转换成我们自写的类对象，最后返回对应的用户信息
        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();

        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        map.put("id", user.getId().toString());
        map.put("username", user.getUsername());
        map.put("photo", user.getPhoto());

        return map;
    }
}
