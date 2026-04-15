package com.kob.backend.controller.pk;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/pk/")
public class BotInfoController {

    @RequestMapping("getbotinfo/")
    public List<String> getBotInfo() {
        List<String> list = new LinkedList<>();
        list.add("sword");
        list.add("tiger");
        list.add("apple");
        return list;
    }
}
