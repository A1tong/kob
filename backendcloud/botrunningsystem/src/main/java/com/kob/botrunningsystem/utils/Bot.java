package com.kob.botrunningsystem.utils;

import java.util.ArrayList;
import java.util.List;

public class Bot implements BotInterface{
    static class Cell {
        public int x, y;
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private boolean check_tail_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};

    // 把操作重放一遍，还原出蛇当前身体的全部格子
    public List<Cell> getCells(int sx, int sy, String steps) {
        List<Cell> res = new ArrayList<>();

        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int i = 0; i < steps.length(); i ++) {
            int d = steps.charAt(i) - '0';

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

    @Override
    public Integer nextMove(String input) {
        String[] strs = input.split("#", -1);

        // 拼地图
        int[][] g = new int[13][14];
        for (int i = 0, k = 0; i < 13; i ++) {
            for (int j = 0; j < 14; j ++, k ++) {
                if (strs[0].charAt(k) == '1') {
                    g[i][j] = 1;
                }
            }
        }

        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);
        List<Cell> aCells = getCells(aSx, aSy, strs[3]);
        List<Cell> bCells = getCells(bSx, bSy, strs[6]);
        for (Cell c: aCells) g[c.x][c.y] = 1;
        for (Cell c: bCells) g[c.x][c.y] = 1;

        for (int i = 0; i < 4; i ++) {
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {
                return i;
            }
        }

        return 0;
    }
}
