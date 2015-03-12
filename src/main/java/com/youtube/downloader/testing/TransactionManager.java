package com.youtube.downloader.testing;

import com.youtube.workerpool.AbstractJob;

/**
 * Created by nareshm on 12/03/2015.
 */
public class TransactionManager extends AbstractJob {
    private static WalletFactory walletFactory;
    private static WalletAdjustManager walletAdjustManager;

    public TransactionManager(String jobName) {
        super(jobName);
    }

    @Override
    public void run() {
        walletFactory = WalletFactory.getInstance();
        walletAdjustManager = walletFactory.createWalletAdjustManager(true);
        walletAdjustManager = walletFactory.createWalletAdjustManager(true);
        walletAdjustManager.adjustWallet();
        walletAdjustManager.adjustWallet();
        walletAdjustManager = walletFactory.createWalletAdjustManager(false);
        walletAdjustManager.adjustWallet();
        walletAdjustManager = walletFactory.createWalletAdjustManager(true);
        walletAdjustManager.adjustWallet();
        walletAdjustManager.adjustWallet();
        walletAdjustManager = walletFactory.createWalletAdjustManager(false);
        walletAdjustManager.adjustWallet();
    }
}
