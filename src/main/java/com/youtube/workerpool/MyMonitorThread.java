package com.youtube.workerpool;

/**
 * Created by nareshm on 12/3/14.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class MyMonitorThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MyMonitorThread.class);
    private ThreadPoolExecutor executor;
    private int seconds;

    private boolean run = true;

    public MyMonitorThread(ThreadPoolExecutor executor) {
        this.executor = executor;
        this.seconds = 40;
    }

    public void shutdown() {
        this.run = false;
    }

    @Override
    public void run() {
        while (run) {
            logger.debug(
                    String.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated()));
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                logger.error("Error:" + e);
            }
        }

    }
}