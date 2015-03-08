package com.youtube.workerpool;

/**
 * Created by nareshm on 3/12/2014.
 */
public class PushJobs {
    public static void main(String[] args) {

        WorkerPool.deployJob(new WorkerThread("final test"));
    }
}
