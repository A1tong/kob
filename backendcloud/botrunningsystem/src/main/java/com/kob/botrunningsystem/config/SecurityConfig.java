package com.kob.botrunningsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration // 声明这个是一个配置类，Spring容器启动时会扫描到它，把它当作配置图纸
@EnableWebSecurity // 开启Spring Security的Web安全功能，没有它Spring Security的过滤器链不会生效
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() // 关闭CSRF（跨站请求伪造）防护，CSRF是防别人伪造你在浏览器上的操作的，靠的是Cookie/Session
                // 设置会话策略为无状态，Spring Security不创建/不使用HttpSession
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests() // 开始配置请求授权规则
                // 保证只有本地服务器使用对应接口，才能访问当前项目的api
                .antMatchers("/bot/add/").hasIpAddress("127.0.0.1")
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().authenticated();
        // 匹配所有OPTIONS请求，放行
        // 因为浏览器发跨域请求前，会先发一个OPTIONS预检请求，如果拦截，跨域就失败了
        // 最后.anyRequest().authenticated()兜底机制，上面没列到的其它所有请求，都必须进行登录认证才放行
        // 而检查的变量就是authentication，这个变量我们之前的后端通过手写JWT验证机制，每次请求都手动填充一次
        // Security检查它然后放行；但是这里没有配置任何登录方式，进一步保证了只有本服务器能使用当前项目的后端api
    }
}
