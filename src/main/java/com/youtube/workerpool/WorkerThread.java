package com.youtube.workerpool;

/**
 * Created by nareshm on 12/3/14.
 */
public abstract class WorkerThread extends AbstractJob {
    private String command;

    public WorkerThread(String s) {
        super(s);
        this.command = s;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start. Command = " + command);
        processCommand();
        System.out.println(Thread.currentThread().getName() + " End.");
    }

    public abstract void processCommand();

    @Override
    public String toString() {
        return this.command;
    }
}
