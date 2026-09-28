package com.kob.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread { // 继承Thread，使得该类可以创建线程
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;
    private final int[][] g;
    private final static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
    private final Player playerA, playerB;
    private Integer nextStepA = null; // A的下一步操作
    private Integer nextStepB = null; // B的下一步操作
    // 锁，会被两名玩家的tomcat线程调用以及共同游戏对局的游戏线程调用
    // 并且最先执行lock.lock()的线程会拿到锁，后面的线程执行到lock.lock()
    // 会被锁住，直到最先拿到锁的线程把锁解开
    private ReentrantLock lock = new ReentrantLock();
    private String status = "playing"; // playing -> finished
    private String loser = ""; // all: 平局，A: A输，B：B输
    private Integer stepA, stepB; // 暂存锁中的移动方向


    public Game(Integer rows, Integer cols, Integer inner_walls_count, Integer idA, Integer idB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
        playerA = new Player(idA, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB, 1, cols - 2, new ArrayList<>());
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    // 在WebSocketServer中调用它，并给它传值，即tomcat线程会使用锁
    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    public int[][] getG() {
        return g;
    }

    private boolean check_connectivity(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return true;
        g[sx][sy] = 1;

        for (int i = 0; i < 4; i ++) {
            int x = sx + dx[i], y = sy + dy[i];

            if (x >= 0 && x < this.rows && y >= 0 && y < this.cols && g[x][y] == 0) {
                if (check_connectivity(x, y, tx, ty)) {
                    g[sx][sy] = 0;
                    return true;
                }
            }
        }

        g[sx][sy] = 0;
        return false;
    }

    private boolean draw() { // 画地图
        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                g[i][j] = 0;

        for (int r = 0; r < this.rows; r++)
            g[r][0] = g[r][this.cols - 1] = 1;
        for (int c = 0; c < this.cols; c++)
            g[0][c] = g[this.rows - 1][c] = 1;

        Random random = new Random();
        for (int i = 0; i < this.inner_walls_count / 2; i++) {
            for (int j = 0; j < 1000; j++) {
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);

                if (g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1) continue;
                if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2) continue;
                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;
                break;
            }
        }

        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);
    }

    public void createMap() {
        for (int i = 0; i < 1000; i ++) {
            if (draw()) break;
        }
    }

    // 加锁实际上就是拿到了可以改的特权，别人只能读我最新写的
    private boolean nextStep() { // 等待两名玩家的下一步操作
        // 开头的sleep(200) → 开局缓冲，给前端渲染地图的时间，只执行一次
        // 前端画一格的时间刚好200ms
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 50; i ++) {
            try { // 先尝试
                Thread.sleep(100); // 该线程睡一秒
                lock.lock(); // 然后拿锁
                try { // 再尝试
                    if (nextStepA != null && nextStepB != null) { // 看看两名玩家是否都有动作
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        stepA = nextStepA;
                        stepB = nextStepB;
                        nextStepA = nextStepB = null;
                        return true;
                    }
                } finally { // 兜底，必须解锁
                    lock.unlock();
                }
            } catch (InterruptedException e) { // 异常就执行
                e.printStackTrace();
            }
        }

        return false; // 代表至少有一个玩家没有动作，表示游戏结束
    }

    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);
        if (cell.x < 0 || cell.x >= rows || cell.y < 0 || cell.y >= cols) return false; // 看看蛇头是否越界
        if (g[cell.x][cell.y] == 1) return false; // 看A的蛇头是否撞墙

        for (int i = 0; i < n - 1; i ++) { // 判断蛇头是否与自己的身体相撞，即判断有没有形成环
            if (cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y) {
                return false;
            }
        }
        // 判断A的蛇头是否与B的某个部位相撞，一定可以证明相撞时刻一定是头先相撞，上面循环也同理
        for (int i = 0; i < n; i ++) {
            if (cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y) {
                return false;
            }
        }

        return true;
    }

    private void judge() { // 判断两条蛇的下一步操作是否合法
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if (!validA || !validB) {
            status = "finished";

            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    private void sendAllMessage(String message) { // 公布结果的具体实现
        // 调用静态成员变量users，通过player的id找到对应的websocket链接，把对应信息发出去
        WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        WebSocketServer.users.get(playerB.getId()).sendMessage(message);
    }

    private void sendMove() { // 向两个Client传递移动信息
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", stepA);
            resp.put("b_direction", stepB);
            sendAllMessage(resp.toJSONString());
        } finally {
            lock.unlock();
        }
    }

    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i ++) {
            for (int j = 0; j < cols; j ++) {
                res.append(g[i][j]);
            }
        }

        return res.toString();
    }

    private void saveToDatabase() {
        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );

        WebSocketServer.recordMapper.insert(record);
    }

    private void sendResult() { // 向两个Client公布结果
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDatabase();
        sendAllMessage(resp.toJSONString());
    }

    @Override // 该类的线程创建后，会自动运行这个函数中的代码
    public void run() {
        for (int i = 0; i < 1000; i ++) {
            if (nextStep()) { // 是否获取了两条蛇的下一步操作
                judge();
                if (status.equals("playing")) {
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                lock.lock();
                try { // 因为涉及到读操作，为了防止数据改写，加个锁
                    if (nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if (nextStepA == null) {
                        loser = "A";
                    } else {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }
    }
}
