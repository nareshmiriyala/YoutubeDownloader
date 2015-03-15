package com.youtube.downloader.testing;

import com.youtube.workerpool.WorkerPool;

/**
 * Created by nareshm on 12/03/2015.
 */
public class WalletTest {
    public static void main(String[] args) {
        WorkerPool.getInstance();
        TransactionManager transactionManager = new TransactionManager();
        WorkerPool.deployJob(transactionManager);
        WorkerPool.deployJob(transactionManager);
        WorkerPool.deployJob(transactionManager);
        WorkerPool.deployJob(transactionManager);
        WorkerPool.deployJob(transactionManager);
        try {
            WorkerPool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
