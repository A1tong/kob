package com.kob.backend.consumer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Integer id;
    private Integer sx; // 屁股横坐标
    private Integer sy; // 屁股纵坐标
    private List<Integer> steps; // 记录每个玩家蛇的每一步指令

    private boolean check_tail_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    // 把操作重放一遍，还原出蛇当前身体的全部格子
    public List<Cell> getCells() {
        List<Cell> res = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int d: steps) {
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            // 如果下一步蛇的长度不增加，则删除屁股格子
            if (!check_tail_increasing(++ step)) {
                res.remove(0);
            }
        }

        return res;
    }

    public String getStepsString() { // 把数字序列，转换成对应的字符串
        StringBuilder res = new StringBuilder();
        for (int d: steps) {
            res.append(d);
        }
        return res.toString();
    }
}
