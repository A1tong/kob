package com.kob.botrunningsystem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread {
    private final ReentrantLock lock = new ReentrantLock(); // 锁
    private final Condition condition = lock.newCondition(); // 环境变量
    private final Queue<Bot> bots = new LinkedList<>(); // 消息队列

    // 增加一个任务至消息队列中，因为Bot里面有botCode，这个就是要跑的代码
    // 一般是tomcat线程执行这个函数，并唤醒BotPool线程
    public void addBot(Integer userId, String botCode, String input) {
        lock.lock();
        try {
            bots.add(new Bot(userId, botCode, input));
            condition.signalAll(); // 唤醒所有由当前环境变量沉睡的线程
        } finally {
            lock.unlock();
        }
    }

    // 注意：这里的超时时间必须大于"编译一次用户代码"的耗时！
    // 写 200ms 时，Consumer 线程会在 joor 还在编译时就被 interrupt，
    // javac 遍历 classpath 上的 jar 时被中断，会抛出一个 message 为 null 的异常，
    // 最终表现成莫名其妙的 "错误: 无法访问com.kob.botrunningsystem.utils" + "null"。
    private void consume(Bot bot) {
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot);
    }

    @Override
    public void run() { // 用这个类开的线程都会运行run方法
        while (true) {
            lock.lock();
            if (bots.isEmpty()) { // 如果消息队列为空
                try {
                    condition.await(); // 通过环境变量挂起线程，让其沉睡，并释放锁
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            } else {
                Bot bot = bots.remove(); // 取队首任务，并将其从消息队列中抹除
                lock.unlock(); // 取完立刻解锁，方便消息队列中的任务增加
                consume(bot); // 执行任务，比较耗时
            }
        }
    }
}
