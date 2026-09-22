package com.kob.backend.service.impl.user.account;

import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.account.LoginService;
import com.kob.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired // authenticationManager的注入是Spring Security提供的登录验证器
    private AuthenticationManager authenticationManager;

    @Override
    public Map<String, String> getToken(String username, String password) {
        //把用户名和密码打包成一张登录申请表
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        //把登录表交给authenticationManager，authenticate函数会自动调用UserDetailsService接口的实现类对象
        //即UserDetailsServiceImpl类对象，调用该对象的方法去与后台数据库中的数据进行比对，只要匹配不成功，直接抛
        //异常，被Spring Security捕获，返回给前端错误信息
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        //匹配成功，把登录成功的用户信息强制转换成自己定义的类UserDetailsImpl继承于UserDetails接口
        //因为spring security只认UserDetails接口
        UserDetailsImpl loginUser = (UserDetailsImpl)authenticate.getPrincipal();
        //UserDetailsImpl里包了一个User对象，取出来并调用JwtUtil类的静态函数createJWT创建jwt令牌
        User user = loginUser.getUser();
        String jwt = JwtUtil.createJWT(user.getId().toString());
        //返回登录成功信息，以及对应的jwt令牌
        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        map.put("token", jwt);

        return map;
    }
}
