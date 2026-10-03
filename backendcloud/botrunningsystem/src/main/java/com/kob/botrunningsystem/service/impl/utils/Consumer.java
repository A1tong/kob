package com.kob.botrunningsystem.service.impl.utils;

import com.kob.botrunningsystem.utils.BotInterface;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class Consumer extends Thread {
    private Bot bot;

    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    // BotPool线程跑这段方法
    public void startTimeout(long timeout, Bot bot) {
        this.bot = bot;
        this.start(); // 开启Consumer线程并行的跑两条线程

        try {
            // BotPool线程最多等timeout / 1000 秒，规定时间内Consumer线程执行完run方法
            // join会正常返回
            this.join(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally { // 否则join返回超时，直接调用这个类的interrupt方法打断Consumer线程的执行，再返回
            this.interrupt();
        }
    }

    // 唯一作用给用户自定义类的名字加上uid后缀，其余均不变
    private String addUid(String code, String uid) {
        String key = "public class Bot"; // 只认“类名”，不管玩家写的是 implements BotInterface 还是全限定名
        int k = code.indexOf(key);
        if (k == -1) { // 找不到类名就原样返回，避免 substring(0, -1) 抛异常把线程弄死
            System.out.println("addUid: 源码里没找到 \"" + key + "\"，未改名");
            return code;
        }
        int p = k + key.length();
        return code.substring(0, p) + uid + code.substring(p);
    }

    @Override
    public void run() {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0, 8);

        // Reflect这个api的作用是得到我们前端用户自己写的源码实例
        // 其参数格式为 => (源码里的包名 + "." + 源码里的 public 类名, 源码)
        // Reflect这个api是joor库自带的，这里为该库的某个类返回了这个Reflect实例对象
        // 这个库是jvm看见下面代码自动拉起来的，joor每次会把没有使用过的类名新增至ClassLoader中
        // 而每次线程运行下面的代码，joor都会去ClassLoader中检查类名，如果有则直接用缓存过的类
        // 导致用户用不到自己想写的类，所以即使线程是临时的，每次类名也必须不一样
        BotInterface botInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.Bot" + uid,
                addUid(bot.getBotCode(), uid) // 用户自己的源码 + 给类名加的随机串
        ).create().get();

        Integer direction = botInterface.nextMove(bot.getInput());
        System.out.println("move-direction: " + bot.getUserId() + " " + direction);

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());

        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
}
