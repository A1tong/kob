package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    // WebSocket是一个多线程的环境，多线程同时读写，可能导致数据错乱、死循环、甚至程序崩溃
    // HashMap不是线程安全的，ConcurrentHashMap是专门为多线程设计的；
    // 总之记住用ConcurrentHashMap就行了，这里创建一个静态变量，管理所有的链接
    final private static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    // 创建一个容器，装载正在排队等待匹配的玩家
    final private static CopyOnWriteArraySet<User> matchpool = new CopyOnWriteArraySet<>();
    private User user;

    // session代表一条WebSocket连接管道，WebSocketServer封装了session
    // 这条连接的信息（sessionId、请求参数、远端地址等）
    // 往这条连接发消息的能力（getBasicRemote().sendText(...)）
    // 关闭连接的能力，都可以通过这个连接管道实现
    private Session session = null;

    private static UserMapper userMapper;

    @Autowired // Spring容器扫到这个标注会自动找Bean对象，注入到函数所需参数中，并调用该函数
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @OnOpen // 连接建立时被触发
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        System.out.println("connected!");
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);

        if (this.user != null) {
            // Tomcat创建WebSocketServer实例，调用onOpen
            // 这个this指的是Tomcat创建的WebSocketServer实例
            users.put(userId, this);
        } else {
            this.session.close();
        }

        System.out.println(users);
    }

    @OnClose // 连接关闭时触发
    public void onClose() {
        // 关闭链接
        System.out.println("disconnected!");
        if (this.user != null) {
            users.remove(this.user.getId());
            matchpool.remove(this.user);
        }
    }

    private void startMatching() {
        System.out.println("start matching!");
        matchpool.add(this.user);

        while(matchpool.size() >= 2) {
            Iterator<User> it = matchpool.iterator();
            User a = it.next(), b = it.next();
            matchpool.remove(a);
            matchpool.remove(b);

            Game game = new Game(13, 14, 20);
            game.createMap();

            JSONObject respA = new JSONObject();
            respA.put("event", "start-matching");
            respA.put("opponent_username", b.getUsername());
            respA.put("opponent_photo", b.getPhoto());
            respA.put("gamemap", game.getG());
            users.get(a.getId()).sendMessage(respA.toJSONString());

            JSONObject respB = new JSONObject();
            respB.put("event", "start-matching");
            respB.put("opponent_username", a.getUsername());
            respB.put("opponent_photo", a.getPhoto());
            respB.put("gamemap", game.getG());
            users.get(b.getId()).sendMessage(respB.toJSONString());
        }
    }

    private void stopMatching() {
        System.out.println("stop matching!");
        matchpool.remove(this.user);
    }

    @OnMessage // 收到消息时触发
    public void onMessage(String message, Session session) { // 一般当作路由
        // 从Client接收消息
        System.out.println("receive_message");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            startMatching();
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        }
    }

    @OnError // 出错时触发
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    // 主动发消息给客户端
    public void sendMessage(String message) {
        // 同步锁，表示同一时刻，只允许一个线程进入这个代码块
        // 即当前链接session，给当前链接发完消息后，解锁锁，给到下一个链接
        synchronized (this.session) {
            try { // 尝试执行下面代码
                // 同步发送，发的时候会阻塞，直到发完
                this.session.getBasicRemote().sendText(message);
            } catch(IOException e) { // catch捕获异常，将异常信息放入IOException这个类实例化的对象e
                e.printStackTrace(); // 将异常信息打印至控制台中
            }
        }
    }
}