package com.kob.backend.config;

import com.kob.backend.config.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // 声明这个是一个配置类，Spring容器启动时会扫描到它，把它当作配置图纸
@EnableWebSecurity // 开启Spring Security的Web安全功能，没有它Spring Security的过滤器链不会生效
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // 直接定义SecurityFilterChain和AuthenticationManager两个@Bean

    @Autowired // 把jwt过滤器Bean实例注入
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean // 注册密码加密器Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // 把AuthenticationManager暴露成Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() // 关闭CSRF（跨站请求伪造）防护，设置会话策略为无状态，Spring Security不创建/不使用HttpSession
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 配置请求授权规则，即哪些请求允许匿名访问本项目后端api，哪些必须登录访问本项目的后端api
                .antMatchers("/user/account/token/", "/user/account/register/").permitAll()
                // 配置请求授权规则，当前放行的url只允许本地服务器拿去访问本项目的后端api
                .antMatchers("/pk/start/game/").hasIpAddress("127.0.0.1")
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().authenticated();
                // 匹配所有OPTIONS请求，放行
                // 因为浏览器发跨域请求前，会先发一个OPTIONS预检请求这个请求不带JWT，如果拦截，跨域就失败了
                // 最后除上面放行的接口外，其他所有请求都必须登录
        // UsernamePasswordAuthenticationFilter是Spring Security官方的表单登录处理器（默认只认POST/login，成功后跳页面）
        // 它作为传统的第一次登录认证，会把用户信息塞入Authentication中，只不过这里被我的LoginController + LoginServiceImpl覆盖了
        // 把JwtAuthenticationTokenFilter插入到Security过滤器链中，放到UsernamePasswordAuthenticationFilter之前
        // 项目中UsernamePasswordAuthenticationFilter没有被启用，只是被当作锚点，为什么选它作为锚点，因为它是官方的第一次登录验证
        // 插在它的前面，表示先验验用户是否有jwt令牌，有的话就不用第一次登录验证了
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override // Spring Security默认会拦截所有WebSocket链接，这里的配置让其放行
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/websocket/**");
    }
} // 这份配置文件会被Spring容器提前规划在蓝图中，防止创建的的Spring Security框架Bean实例与用户需要的不一致