package com.kob.backend.controller.user.account;

import com.kob.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//@component：标识这个类是一个Spring Bean，Spring容器会把它实例化为Bean对象加入容器中，通过@Autowire，@Resource
//等方式注入使用；
//@RequestMapping作用：它可以加在类上表示该类所有请求的公共路径前缀；它可以加在方法上表示具体某个请求的路径和请求方式
//@GetMapping作用：@RequestMapping的快捷方式，专门用于处理GET请求，还有Post/PutMapping
//两个注解的组合：@Controller和@ResponseBody，前者本质是@component的派生注解，所以会被Spring扫描并注册为Bean
//作用是：配合@RequestMapping/@GetMapping等注解用来接收和处理HTTP请求，默认把控制器方法的返回值当作逻辑视图名
//通过ViewResolver解析找到匹配的html文件或其它类型的文件，最后返回给前端；后者作用：让方法的返回值直接作为HTTP响
//应体返回，不再经过视图解析器；
@RestController
public class RegisterController {
    @Autowired
    private RegisterService registerService; // 把实例化注册接口的类对象的注入

    //@RequestParam：Spring MVC利用它把HTTP请求参数绑定到控制器方法参数
    //当@RequestParam直接修饰一个Map<String, String>时，Spring会把当前请求中的所有参数收集到这个Map中
    @PostMapping("/user/account/register/")
    public Map<String, String> register(@RequestParam Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        String confirmedPassword = map.get("confirmedPassword");
        return registerService.register(username, password, confirmedPassword);
        //根据类对象自带的判断返回最终结果给前端用户
    }
} // 注册板块的接客员，封装了具体的业务逻辑