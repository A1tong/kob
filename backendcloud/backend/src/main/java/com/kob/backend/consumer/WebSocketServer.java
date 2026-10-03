package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
    public final static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    private User user;

    // session代表一条WebSocket连接管道，WebSocketServer封装了session
    // 这条连接的信息（sessionId、请求参数、远端地址等）
    // 往这条连接发消息的能力（getBasicRemote().sendText(...)）
    // 关闭连接的能力，都可以通过这个连接管道实现
    private Session session = null;

    private static UserMapper userMapper;
    public static RecordMapper recordMapper;
    private static BotMapper botMapper;
    public static RestTemplate restTemplate;

    public Game game = null;
    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";

    // Spring容器扫到这个标注会按方法参数的类型从容器里找到匹配的Bean，把它作为实参反射调用该方法
    // 前提是这个方法所在的类本身就是Spring Bean，且每个Bean的这类方法只在启动阶段执行一次
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper) { WebSocketServer.botMapper = botMapper; }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) { WebSocketServer.restTemplate = restTemplate; }

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
            stopMatching();
        }
    }

    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {
        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);
        Bot botA = botMapper.selectById(aBotId), botB = botMapper.selectById(bBotId);

        // 这里提前把两个游戏对象A和B传入game实例中，为后面创建游戏线程，供两人对战做准备
        Game game = new Game(
                13,
                14,
                20,
                a.getId(),
                botA,
                b.getId(),
                botB
        );
        game.createMap();
        if (users.get(a.getId()) != null)
            users.get(a.getId()).game = game; // 把创建的game，分配给所有参与者
        if (users.get(b.getId()) != null)
            users.get(b.getId()).game = game;

        // 首先这个类的所有代码都是tomcat给予的线程在运行
        // tomcat线程为了尽快解放，它会调用game.start()创建了一个新的线程
        // 这个线程会去运行上面Game类的实例game的run方法，使得A和B开始对战
        // 补充一点，这个游戏线程一般都是最后一个匹上的人的tomcat线程创建的
        // 他把与他匹配的人都拉入这个游戏线程之中
        game.start();

        // tomcat线程将两名玩家的信息打包，通过链接各自发送出去
        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        JSONObject respA = new JSONObject();
        respA.put("event", "start-matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        if (users.get(a.getId()) != null)
            users.get(a.getId()).sendMessage(respA.toJSONString());

        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        if (users.get(b.getId()) != null)
            users.get(b.getId()).sendMessage(respB.toJSONString());
    }

    private void startMatching(Integer botId) {
        System.out.println("start matching!");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        data.add("bot_id", botId.toString());
        // RestTemplate这个api类是Spring提供的，Spring Boot这个基于Sping的服务机器
        // 自动帮我们把这个api类引入了，这里的作用是：向addPlayerUrl发一个HTTP POST请求
        // 把data作为请求体发过去，等对方响应，再把响应体当成String返回
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }

    private void stopMatching() {
        System.out.println("stop matching!");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    private void move(int direction) {
        // 通过调用game对象的getPlayer方法得到对应的参与者，调用参与者的getId得到
        // 参与者是数据库中的哪个用户，把用户id与当前类中的用户id进行比对，看是否相同
        // 相同则，给与下一步行动
        if (game.getPlayerA().getId().equals(user.getId())) {
            if (game.getPlayerA().getBotId().equals(-1)) // 真人操作
                game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            if (game.getPlayerB().getBotId().equals(-1)) // 真人操作
                game.setNextStepB(direction);
        }
    }

    @OnMessage // 收到消息时触发
    public void onMessage(String message, Session session) { // 一般当作路由
        // 从Client接收消息
        System.out.println("receive_message");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            Integer botId = data.getInteger("bot_id");
            if (botId == null) botId = -1;  // 前端没传 bot_id 时按“亲自上阵”处理，避免空指针
            startMatching(botId);
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
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