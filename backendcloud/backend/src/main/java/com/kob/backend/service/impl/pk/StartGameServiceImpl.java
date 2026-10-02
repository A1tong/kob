package com.kob.backend.service.impl.pk;

import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.service.pk.StartGameService;
import org.springframework.stereotype.Service;

// 实现业务逻辑类，接受匹配池匹配成功的两个人的信息，并进行处理
@Service
public class StartGameServiceImpl implements StartGameService {
    @Override
    public String startGame(Integer aId, Integer bId) {
        System.out.println("start game:" + aId + " " + bId);
        WebSocketServer.startGame(aId, bId);
        return "start game success";
    }
}
